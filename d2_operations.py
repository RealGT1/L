import json
import logging
import uuid  # Add this import for generating UIDs
from collections import Counter # Import Counter for error summary
from sqlalchemy import select, insert, update, Table, MetaData, text, PrimaryKeyConstraint, inspect # Added inspect
from sqlalchemy.exc import SQLAlchemyError
from db.d2_models import engine, Session, get_or_create_table, get_all_tables, find_table_by_kind_service, D2SkippedData, init_d2_db # Added init_d2_db

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def find_references(data_obj, parent_service=None):
    """
    Find all objects with a 'kind' field in the data object
    Returns a list of references with their kind, name, namespace, tenant, and uid
    
    Args:
        data_obj: The object to extract references from
        parent_service: The service of the parent object (used for context)
        
    Returns:
        List of reference objects
    """
    references = []
    
    def _extract_refs(obj, path=""):
        if isinstance(obj, dict):
            # Check if this is a reference (has 'kind' field)
            if 'kind' in obj and isinstance(obj.get('kind'), str):
                # Store both original and normalized kind
                original_kind = obj.get('kind')
                normalized_kind = normalize_kind(original_kind)
                
                # Extract service from the reference if available
                ref_service = obj.get('service', parent_service)
                
                ref = {
                    'kind': normalized_kind,  # Store normalized kind as the primary kind
                    'original_kind': original_kind,  # Store original kind for reference
                    'name': obj.get('name'),
                    'namespace': obj.get('namespace'),
                    'uid': obj.get('uid'),
                    'tenant': obj.get('tenant'),  # Capture tenant information
                    'service': ref_service,  # Use parent's service as default
                    'path': path
                }
                if ref['kind'] and (ref['uid'] or (ref['name'] and ref['namespace'])):
                    references.append(ref)
            
            # Also look for reference arrays like "any_name": [{"kind": "endpoint", ...}]
            for key, value in obj.items():
                if isinstance(value, list):
                    for item in value:
                        if isinstance(item, dict) and 'kind' in item and isinstance(item.get('kind'), str):
                            # Store both original and normalized kind
                            original_kind = item.get('kind')
                            normalized_kind = normalize_kind(original_kind)
                            
                            # Extract service or use parent's
                            ref_service = item.get('service', parent_service)
                            
                            ref = {
                                'kind': normalized_kind,  # Store normalized kind as the primary kind
                                'original_kind': original_kind,  # Store original kind for reference
                                'name': item.get('name'),
                                'namespace': item.get('namespace'),
                                'uid': item.get('uid'),
                                'tenant': item.get('tenant'),  # Capture tenant information
                                'service': ref_service,  # Use parent's service as default
                                'path': f"{path}.{key}" if path else key
                            }
                            if ref['kind'] and (ref['uid'] or (ref['name'] and ref['namespace'])):
                                references.append(ref)
            
            # Special handling for namespace data - FIXED VERSION
            # This now handles both direct "system_metadata.namespace" and array indices like "system_metadata.namespace[0]"
            if (path == "system_metadata.namespace" or 
                path.startswith("system_metadata.namespace[") or
                (path == "system_metadata" and "namespace" in obj and isinstance(obj["namespace"], list))):
                
                # If we're at the system_metadata level and it contains namespace array
                if path == "system_metadata" and "namespace" in obj and isinstance(obj["namespace"], list):
                    namespace_items = obj["namespace"]
                # Otherwise if we're already at the namespace array or item
                elif isinstance(obj, list):
                    namespace_items = obj
                else:
                    namespace_items = [obj]
                
                # Process all namespace items
                for item in namespace_items:
                    if isinstance(item, dict) and item.get('kind') == 'namespace':
                        # Extract service or use parent's
                        ref_service = item.get('service', parent_service)
                        
                        ref = {
                            'kind': 'namespace',
                            'original_kind': 'namespace',  # Original and normalized are the same here
                            'name': item.get('name'),
                            'namespace': 'system',  # Default namespace for namespaces
                            'uid': item.get('uid'),
                            'tenant': item.get('tenant'),  # Capture tenant information
                            'service': ref_service,  # Use parent's service as default
                            'path': path if path.startswith("system_metadata.namespace") else "system_metadata.namespace"
                        }
                        if ref['uid'] or ref['name']:
                            references.append(ref)
            
            # Recursively search nested dictionaries
            for key, value in obj.items():
                new_path = f"{path}.{key}" if path else key
                _extract_refs(value, new_path)
        
        elif isinstance(obj, list):
            # Recursively search list items
            for i, item in enumerate(obj):
                new_path = f"{path}[{i}]"
                _extract_refs(item, new_path)
    
    _extract_refs(data_obj)
    return references


def extract_kind_from_key(key):
    """Extract kind from object key path"""
    # Example key: /akar/db/ves.io.schema.advertise_policy.Object.default/primary/uid
    if not key or not isinstance(key, str):
        return None
    
    parts = key.split('/')
    if len(parts) < 4:
        return None
    
    try:
        # Extract schema part
        schema_part = parts[3]  # ves.io.schema.advertise_policy.Object.default
        
        # Split schema into components
        schema_components = schema_part.split('.')
        
        # Look for the schema type before "Object"
        for i, comp in enumerate(schema_components):
            if comp == "Object" and i > 0:
                # Return the component before "Object"
                kind = schema_components[i-1]
                logger.debug(f"Extracted kind '{kind}' from key: {key}")
                return kind
                
        # If we didn't find "Object", try another approach - use the last component
        if len(schema_components) > 1:
            potential_kind = schema_components[-2]  # Try second-to-last component
            if potential_kind and potential_kind != "default" and potential_kind != "schema":
                logger.debug(f"Extracted fallback kind '{potential_kind}' from key: {key}")
                return potential_kind
    except Exception as e:
        logger.error(f"Error extracting kind from key '{key}': {e}")
    
    return None



def extract_service_from_key(key):
    """
    Extract service from object key path
    
    Example: 
    - Input key: "/vulpix/db/ves.io.pikachu.version.Object.default/primary/uid"
    - Output service: "vulpix"
    
    Args:
        key: The full object key path
        
    Returns:
        str: Extracted service name or None if not found
    """
    # Check for valid key
    if not key or not isinstance(key, str):
        return None
    
    # Split the key into path segments
    parts = key.split('/')
    
    # The service should be in the first non-empty segment
    # Usually at index 1 in the path /service/db/...
    for part in parts:
        if part and part != "":
            return part
    
    return None


def normalize_kind(kind):
    """Enhanced normalization with better debugging"""
    original = kind
    
    if not kind or not isinstance(kind, str):
        logger.warning(f"normalize_kind received invalid input: {kind}")
        return kind
        
    # Check if this is already a simple kind
    if '.' not in kind:
        return kind
    
    # Special case for known problematic kinds
    if "service_policy_set" in kind:
        logger.info(f"Found service_policy_set in {kind}, normalizing")
        return "service_policy_set"
        
    # Handle schema path pattern like "ves.io.schema.endpoint.Object"
    parts = kind.split('.')
    
    # Debug the parts
    logger.debug(f"Kind parts: {parts}")
    
    # Look for the schema type before "Object"
    for i, part in enumerate(parts):
        if part == "Object" and i > 0:
            # Return the component before "Object"
            result = parts[i-1]
            logger.debug(f"Normalized {original} -> {result} (before Object)")
            return result
    
    # If we don't find "Object" in the schema path, try to extract
    # the last meaningful component that's not "schema" or "default"
    for part in reversed(parts):
        if part and part not in ["Object", "schema", "default", "io", "ves"]:
            logger.debug(f"Normalized {original} -> {part} (fallback)")
            return part
            
    # If all else fails, return the original
    logger.warning(f"Failed to normalize {original}, returning as is")
    return kind


def process_data_to_d2_with_missing_fields_handling(raw_data_list):
    """
    Enhanced process to transform data from D1 to D2 with special handling for records
    with missing namespace or name/UID fields, optimized with batch processing for memory efficiency
    """
    import gc  # Import garbage collector for explicit memory management
    
    # Ensure the D2 database and necessary tables (like d2_skipped_data) are initialized
    try:
        init_d2_db() 
        # Verify d2_skipped_data table exists after init
        inspector = inspect(engine)
        if not inspector.has_table(D2SkippedData.__tablename__):
             logger.error(f"Table '{D2SkippedData.__tablename__}' still does not exist after init_d2_db().")
             # Optionally raise an error or handle appropriately
             # raise RuntimeError(f"Failed to create required table: {D2SkippedData.__tablename__}")
        else:
             logger.info(f"Table '{D2SkippedData.__tablename__}' confirmed to exist.")
             
    except Exception as e:
        logger.error(f"Failed to initialize D2 database: {e}")
        # Decide if you want to proceed without the skipped table or stop
        # return # Example: Stop processing if DB init fails

    if not raw_data_list:
        logger.warning("No data provided to process_data_to_d2")
        return
        
    # Define batch size for processing
    BATCH_SIZE = 10000  # Adjust this value based on your system's memory capacity
    initial_total_items = len(raw_data_list) # Store initial count
    total_batches = (initial_total_items + BATCH_SIZE - 1) // BATCH_SIZE  # Ceiling division
    
    logger.info(f"Processing {initial_total_items} objects from D1 to D2 with missing fields handling (in {total_batches} batches)")
    
    # Initialize global tracking data structures (preserved across batches)
    namespace_cache = {}  # Cache for namespace UIDs by (name, tenant)
    error_records = {}    # Will store detailed errors by key
    
    # Global counters for final reporting
    total_processed_items = 0
    total_skipped_items = 0 # Renamed from total_error_items for clarity
    total_fixed_namespace_items = 0
    total_fixed_name_uid_items = 0
    skipped_by_error_type = Counter() # Use Counter for easy counting

    # Helper function to save to D2SkippedData with required composite key fields
    def save_to_d2_skipped_data(key, kind=None, service=None, name=None, uid=None, 
                               namespace=None, tenant=None, error_type="unknown_error", 
                               error_message="", size_bytes=0):
        # Ensure we have valid values for the composite key fields
        record_uid = uid if uid else f"generated-{str(uuid.uuid4())}"
        record_service = service if service else "unknown"
        record_object_type = kind if kind else "unknown"  # Using 'kind' as 'object_type'
        
        # Increment global skipped counter and error type counter
        nonlocal total_skipped_items
        total_skipped_items += 1
        skipped_by_error_type[error_type] += 1
        
        skipped_session = Session()
        try:
            # Corrected: Removed name, namespace, tenant from the constructor call
            skipped_data = D2SkippedData(
                uid=record_uid,
                service=record_service,
                object_type=record_object_type,
                key=key,
                size_bytes=size_bytes, # Corrected variable name
                error_type=error_type,
                error_message=error_message[:1000] 
            )
            skipped_session.add(skipped_data)
            skipped_session.commit()
        except Exception as e:
            skipped_session.rollback()
            # Log the specific record details that failed
            logger.error(f"Error saving skipped data record (uid={record_uid}, service={record_service}, object_type={record_object_type}): {e}")
        finally:
            skipped_session.close()
    
    # Process data in batches
    for batch_num, batch_start in enumerate(range(0, initial_total_items, BATCH_SIZE)):
        batch_end = min(batch_start + BATCH_SIZE, initial_total_items)
        current_batch = raw_data_list[batch_start:batch_end]
        
        logger.info(f"Processing batch {batch_num+1}/{total_batches} (items {batch_start} to {batch_end-1})")
        
        # Per-batch data structures that will be cleared after each batch
        objects_by_kind_service = {}  # Objects grouped by kind and service
        kind_service_references = {}  # References by kind and service
        
        # Batch counters - These are useful for per-batch logging but totals are tracked globally
        batch_processed_items_counter = 0 # Use a different name to avoid confusion with global
        batch_skipped_items_counter = 0
        batch_fixed_namespace_items_counter = 0
        batch_fixed_name_uid_items_counter = 0
        
        # Pre-process pass: Extract all objects, get namespaces, normalize kinds, and collect references
        logger.info(f"Batch {batch_num+1}: Initial pass - collecting objects, namespaces, and references...")
        
        for item_index, item in enumerate(current_batch):
            # Global index for logging purposes
            global_item_index = batch_start + item_index
            
            key = item.get('key', '')
            value = item.get('value')
            
            # Handle size field consistently
            size = item.get('size')  # Get size from the item
            if not size:
                size = item.get('size_bytes')
            if not size:
                size = item.get('size(Bytes)')
            # Ensure size is an integer or None
            try:
                size = int(size) if size is not None else None
            except (ValueError, TypeError):
                logger.warning(f"Invalid size value '{size}' for item #{global_item_index}, setting to None: {key}")
                size = None

            # Skip items without proper value
            if not isinstance(value, dict):
                logger.warning(f"Skipping item #{global_item_index} with invalid value: {key}")
                batch_skipped_items_counter += 1
                error_records[key] = {
                    "value": value,
                    "error": "Invalid value (not a dictionary)",
                    "item_index": global_item_index
                }
                
                # Extract object_type from item if available
                object_type = item.get('object_type', 'unknown')
                item_uid = item.get('uid', f"generated-{str(uuid.uuid4())}")
                
                # Use helper function to save to D2SkippedData
                save_to_d2_skipped_data(
                    key=key,
                    uid=item_uid,
                    kind=object_type,
                    service=item.get('service', 'unknown'),
                    error_type="invalid_value",
                    error_message="Invalid value (not a dictionary)",
                    size_bytes=size
                )
                
                continue
                
            # Extract basic information
            metadata = value.get('metadata', {})
            system_metadata = value.get('system_metadata', {})
            
            # Extract service from item or key
            service = item.get('service')
            if not service:
                service = extract_service_from_key(key)
                if not service:
                    logger.warning(f"Could not extract service for item #{global_item_index}: {key}")
                    batch_skipped_items_counter += 1
                    error_records[key] = {
                        "value": value,
                        "error": "Could not extract service",
                        "item_index": global_item_index
                    }
                    
                    # Use helper function to save to D2SkippedData
                    save_to_d2_skipped_data(
                        key=key,
                        error_type="missing_service",
                        error_message="Could not extract service",
                        size_bytes=size
                    )
                    
                    continue
            
            # Try to get kind from multiple possible locations
            original_kind = value.get('kind')
            object_type = item.get('object_type')
            if not original_kind and object_type:
                original_kind = object_type
            
            if not original_kind:
                # Try to find kind in metadata
                original_kind = metadata.get('kind')
                
                # Try to extract from key if still not found
                if not original_kind:
                    original_kind = extract_kind_from_key(key)
                    
                    # If still not found but has system_metadata.owner_view.kind, use that
                    if not original_kind and system_metadata and 'owner_view' in system_metadata:
                        owner_view = system_metadata.get('owner_view', {})
                        original_kind = owner_view.get('kind')
            
            # Skip if we can't determine the kind
            if not original_kind:
                logger.warning(f"Skipping item #{global_item_index} with unknown kind: {key}")
                batch_skipped_items_counter += 1
                error_records[key] = {
                    "value": value,
                    "error": "Could not determine kind",
                    "item_index": global_item_index
                }
                
                # Use helper function to save to D2SkippedData
                save_to_d2_skipped_data(
                    key=key,
                    service=service,
                    error_type="unknown_kind",
                    error_message="Could not determine kind",
                    size_bytes=size
                )
                
                continue
            
            # Normalize the kind (once)
            normalized_kind = normalize_kind(original_kind)
            
            name = metadata.get('name')
            uid = metadata.get('uid')
            namespace = metadata.get('namespace')
            
            # Extract tenant from system_metadata
            tenant = None
            if system_metadata:
                tenant = system_metadata.get('tenant')

            # Special handling for namespace objects - identify and cache them
            is_namespace = normalized_kind == 'namespace' or object_type == 'namespace'
            if is_namespace:
                # Namespaces don't have a namespace field in their metadata
                # Set a default namespace for them
                if name and uid and not namespace:
                    namespace = 'system'  # Default namespace for namespaces
                    logger.debug(f"Setting default namespace 'system' for namespace object: {name}")
                    
                # Cache namespace UIDs for later use - keep this cache across batches
                if name and uid:
                    # For namespace objects, don't include service in the cache key
                    cache_key = (name, tenant) if tenant else (name, None)
                    namespace_cache[cache_key] = uid
                    logger.debug(f"Cached namespace: {name}, tenant: {tenant}, UID: {uid} (shared across services)")
                    
                # For namespace objects, we'll use the common 'namespace' table
                service = None
            
            # FIX 1: Handle missing name or UID
            fixed_name_uid_this_item = False # Track fix for this item
            if not (name and uid):
                # Try to extract from system_metadata
                if system_metadata and 'uid' in system_metadata:
                    uid = system_metadata.get('uid')
                    
                # For StatusObjects or similar objects that may not have a name
                if not name and normalized_kind.lower().endswith('statusobject'):
                    if uid:
                        # Use a derived name based on the UID for StatusObjects
                        name = f"status-{uid[:8]}"
                        logger.info(f"Generated name '{name}' for StatusObject with UID {uid}")
                        fixed_name_uid_this_item = True
                    else:
                        # If we can extract a UID from the key path as last resort
                        key_parts = key.split('/')
                        if len(key_parts) > 0:
                            potential_uid = key_parts[-1]
                            if len(potential_uid) > 8:  # Simple validation for UID-like string
                                uid = potential_uid
                                name = f"derived-{potential_uid[:8]}"
                                logger.info(f"Extracted UID '{uid}' and generated name '{name}' from key: {key}")
                                fixed_name_uid_this_item = True
            
            # Increment global counter if fixed
            if fixed_name_uid_this_item:
                total_fixed_name_uid_items += 1
                batch_fixed_name_uid_items_counter += 1

            # Still missing name or UID after fix attempts?
            if not (name and uid):
                logger.warning(f"Still missing name or UID after fix attempts for item #{global_item_index}: {key}")
                batch_skipped_items_counter += 1
                error_records[key] = {
                    "value": value,
                    "error": "Still missing name or UID after fix attempts",
                    "metadata": metadata,
                    "system_metadata": system_metadata,
                    "item_index": global_item_index
                }
                
                # Use helper function to save to D2SkippedData
                save_to_d2_skipped_data(
                    key=key,
                    kind=normalized_kind,
                    service=service,
                    name=name,
                    uid=uid,
                    tenant=tenant,
                    error_type="missing_name_uid",
                    error_message="Still missing name or UID after fix attempts",
                    size_bytes=size
                )
                
                continue
            
            # FIX 2: Handle missing namespace
            fixed_namespace_this_item = False # Track fix for this item
            if not namespace:
                # Check system_metadata.namespace array
                if system_metadata and 'namespace' in system_metadata:
                    ns_data = system_metadata.get('namespace')
                    if isinstance(ns_data, list) and len(ns_data) > 0 and isinstance(ns_data[0], dict):
                        namespace = ns_data[0].get('name')
                        logger.info(f"Extracted namespace '{namespace}' from system_metadata.namespace")
                        fixed_namespace_this_item = True
                
                # If not found and this is a deployment or status object, try setting default namespace
                if not namespace and ('deployment' in normalized_kind.lower() or 'status' in normalized_kind.lower()):
                    namespace = 'system'
                    logger.info(f"Setting default namespace 'system' for {normalized_kind}: {name}")
                    fixed_namespace_this_item = True
                    
                # Try to extract from key path
                if not namespace:
                    # Check if path has namespace information (common patterns in the system)
                    if 'namespace/' in key:
                        path_parts = key.split('namespace/')
                        if len(path_parts) > 1:
                            potential_ns = path_parts[1].split('/')[0]
                            if potential_ns:
                                namespace = potential_ns
                                logger.info(f"Extracted namespace '{namespace}' from key path: {key}")
                                fixed_namespace_this_item = True
                                
                # Handle application objects with no namespace in metadata or system_metadata
                if not namespace and 'application' in normalized_kind.lower():
                    # For application objects, often they belong to "system" namespace by default
                    namespace = 'system'
                    logger.info(f"Setting default namespace 'system' for application object: {key}")
                    fixed_namespace_this_item = True

                # Extract namespace from the key if possible
                if not namespace:
                    # Try to find namespace in the key path using common patterns
                    key_parts = key.split('/')
                    
                    # Pattern: .../namespace/NAME/...
                    for i, part in enumerate(key_parts):
                        if part == 'namespace' and i + 1 < len(key_parts):
                            namespace = key_parts[i + 1]
                            logger.info(f"Extracted namespace '{namespace}' from key path at position {i+1}")
                            fixed_namespace_this_item = True
                            break
                    
                    # Pattern: .../by-namespace/NAME/...
                    if not namespace:
                        for i, part in enumerate(key_parts):
                            if part == 'by-namespace' and i + 1 < len(key_parts):
                                namespace = key_parts[i + 1]
                                logger.info(f"Extracted namespace '{namespace}' from key path at position {i+1}")
                                fixed_namespace_this_item = True
                                break
                    
                    # Maurice-specific pattern: Objects in maurice service often belong to system namespace
                    if not namespace and 'maurice' in service.lower():
                        namespace = 'system'
                        logger.info(f"Setting default namespace 'system' for maurice service object: {key}")
                        fixed_namespace_this_item = True

                # For kubernetes-related objects, they're often in system namespace
                if not namespace and isinstance(value, dict):
                    # Check spec structure for kubernetes configuration without relying on object name
                    has_kubernetes = False
                    
                    # Look through the object structure for kubernetes configurations
                    if (isinstance(value.get('spec'), dict) and 
                        isinstance(value.get('spec').get('app_spec'), dict) and
                        isinstance(value.get('spec').get('app_spec').get('App'), dict)):
                        
                        app_spec = value.get('spec').get('app_spec').get('App', {})
                        # Check if any key in app_spec is 'kubernetes'
                        if 'kubernetes' in app_spec:
                            has_kubernetes = True
                    
                    # Also check for kubernetes in any labels or annotations
                    if not has_kubernetes and isinstance(metadata, dict):
                        if isinstance(metadata.get('labels'), dict):
                            for k, v in metadata.get('labels', {}).items():
                                if 'kubernetes' in str(k).lower() or 'kubernetes' in str(v).lower():
                                    has_kubernetes = True
                                    break
                        
                        if not has_kubernetes and isinstance(metadata.get('annotations'), dict):
                            for k, v in metadata.get('annotations', {}).items():
                                if 'kubernetes' in str(k).lower() or 'kubernetes' in str(v).lower():
                                    has_kubernetes = True
                                    break
                    
                    if has_kubernetes:
                        namespace = 'system'
                        logger.info(f"Setting default namespace 'system' for kubernetes-related object: {key}")
                        fixed_namespace_this_item = True

                # Set a default tenant namespace if there's a tenant in system_metadata
                if not namespace and system_metadata and 'tenant' in system_metadata and system_metadata['tenant']:
                    tenant_value = system_metadata['tenant']
                    namespace = f"tenant-{tenant_value}"
                    logger.info(f"Setting tenant namespace '{namespace}' based on tenant: {tenant_value}")
                    fixed_namespace_this_item = True
            
            # Increment global counter if fixed
            if fixed_namespace_this_item:
                total_fixed_namespace_items += 1
                batch_fixed_namespace_items_counter += 1

            # Still missing namespace after fix attempts?
            if not namespace:
                logger.warning(f"Still missing namespace after fix attempts for item #{global_item_index}: {key}")
                batch_skipped_items_counter += 1
                error_records[key] = {
                    "value": value,
                    "error": "Still missing namespace after fix attempts",
                    "metadata": metadata,
                    "system_metadata": system_metadata,
                    "item_index": global_item_index
                }
                
                # Use helper function to save to D2SkippedData
                save_to_d2_skipped_data(
                    key=key,
                    kind=normalized_kind,
                    service=service,
                    name=name,
                    uid=uid,
                    tenant=tenant,
                    error_type="missing_namespace",
                    error_message="Still missing namespace after fix attempts",
                    size_bytes=size
                )
                
                continue
                
            # Find all references in the object (do this only once)
            references = find_references(value, service)
            
            # Extract reference kinds for table schema generation
            ref_kinds = set()
            for ref in references:
                ref_kind = ref.get('kind')  # This is already normalized by find_references
                if ref_kind and ref_kind != normalized_kind:  # Don't include self-references by kind
                    ref_kinds.add(ref_kind)
            
            # Always add namespace reference for non-namespace objects
            if not is_namespace:
                ref_kinds.add('namespace')
            
            # Update kind_service_references - use kind+service as the key
            kind_service_key = (normalized_kind, service)
            if kind_service_key not in kind_service_references:
                kind_service_references[kind_service_key] = set()
            kind_service_references[kind_service_key].update(ref_kinds)
            
            # Store object info - grouped by kind+service
            if kind_service_key not in objects_by_kind_service:
                objects_by_kind_service[kind_service_key] = []
                
            objects_by_kind_service[kind_service_key].append({
                'key': key,
                'uid': uid,
                'name': name,
                'namespace': namespace,
                'tenant': tenant,
                'service': service,
                'value': value,
                'size': size,
                'original_kind': original_kind,
                'references': references  # Store references to avoid re-extraction
            })
        
        # Log progress for this batch
        logger.info(f"Batch {batch_num+1}: Found {len(objects_by_kind_service)} different kind-service combinations")
        logger.info(f"Batch {batch_num+1}: Fixed {batch_fixed_namespace_items_counter} items with missing namespace")
        logger.info(f"Batch {batch_num+1}: Fixed {batch_fixed_name_uid_items_counter} items with missing name/UID")
        logger.info(f"Batch {batch_num+1}: Skipped {batch_skipped_items_counter} items in pre-processing")

        # Phase 1: Create tables and load data for this batch
        logger.info(f"Batch {batch_num+1}: Phase 1 - Creating tables and loading data...")
        
        batch_processed_count = 0 # Counter for successful Phase 1 processing in this batch
        
        try:
            for (kind, service), objects in objects_by_kind_service.items():
                # Get references for this kind+service
                references = list(kind_service_references.get((kind, service), set()))
                
                # Create or get table with service-aware naming
                try:
                    table = get_or_create_table(kind, service, references)
                except Exception as e:
                    logger.error(f"Failed to create/get table for kind {kind}, service {service or 'default'}: {e}")
                    # Mark all objects of this kind/service as failed
                    for obj in objects:
                        key = obj['key']
                        error_records[key] = {
                            "value": obj['value'],
                            "error": f"Failed to create/get table for kind {kind}, service {service or 'default'}: {str(e)}",
                            "kind": kind,
                            "service": service
                        }
                        # Save to skipped data
                        save_to_d2_skipped_data(
                            key=key, kind=kind, service=service, name=obj.get('name'), uid=obj.get('uid'),
                            namespace=obj.get('namespace'), tenant=obj.get('tenant'),
                            error_type="table_creation_error", error_message=str(e), size_bytes=obj.get('size')
                        )
                    continue # Skip processing objects for this kind/service in this batch
                
                # Insert data for all objects of this kind - PROCESS EACH OBJECT IN A SEPARATE TRANSACTION
                for obj in objects:
                    # Use a fresh session for each object to avoid transaction issues
                    obj_session = Session()
                    
                    try:
                        # Common data for insert
                        data = {
                            'uid': obj['uid'],
                            'name': obj['name'],
                            'namespace': obj['namespace'],
                            'tenant': obj['tenant'],
                            'service': obj['service'] or '',  # Ensure service is never NULL
                            'raw_key': obj['key'],
                            'size_bytes': obj['size']
                        }
                        
                        # Extract timestamps from system_metadata if available
                        system_metadata = obj['value'].get('system_metadata', {})
                        from datetime import datetime, timezone
                        
                        if system_metadata:
                            # Extract creation timestamp
                            if 'creation_timestamp' in system_metadata:
                                ts = system_metadata['creation_timestamp']
                                if isinstance(ts, dict) and 'seconds' in ts:
                                    # Convert to UTC datetime
                                    data['created_at'] = datetime.fromtimestamp(ts['seconds'], tz=timezone.utc)
                            
                            # Extract modification timestamp
                            if 'modification_timestamp' in system_metadata:
                                ts = system_metadata['modification_timestamp']
                                if isinstance(ts, dict) and 'seconds' in ts:
                                    # Convert to UTC datetime
                                    data['updated_at'] = datetime.fromtimestamp(ts['seconds'], tz=timezone.utc)
                        
                        # Check if record exists
                        existing = obj_session.execute(
                            select(table).where(table.c.uid == obj['uid'])
                        ).fetchone()
                        
                        if existing:
                            # Update existing record
                            obj_session.execute(
                                update(table).where(table.c.uid == obj['uid']).values(**data)
                            )
                        else:
                            # Insert new record
                            obj_session.execute(
                                insert(table).values(**data)
                            )
                        
                        # Commit this object immediately
                        obj_session.commit()
                        batch_processed_count += 1
                        total_processed_items += 1 # Increment global counter
                        
                        if batch_processed_count % 100 == 0:
                            logger.info(f"Batch {batch_num+1}: Phase 1 - Processed {batch_processed_count} objects")
                        
                    except Exception as e:
                        # If any error occurs, roll back this object's transaction only
                        obj_session.rollback()
                        logger.error(f"Error processing object {kind}/{obj['name']} (service: {service or 'default'}): {e}")
                        
                        # Track this error
                        error_records[obj['key']] = {
                            "value": obj['value'],
                            "error": f"Database error: {str(e)}",
                            "kind": kind,
                            "service": service,
                            "name": obj['name'],
                            "uid": obj['uid'],
                            "namespace": obj['namespace']
                        }
                        
                        # Use helper function to save to D2SkippedData
                        save_to_d2_skipped_data(
                            key=obj['key'],
                            kind=kind,
                            service=service,
                            name=obj['name'],
                            uid=obj['uid'],
                            namespace=obj['namespace'],
                            tenant=obj['tenant'],
                            error_type="database_error",
                            error_message=str(e),
                            size_bytes=obj['size']
                        )

                    finally:
                        # Close the session for this object
                        obj_session.close()
            
            logger.info(f"Batch {batch_num+1}: Phase 1 - Successfully processed {batch_processed_count} objects into D2 database")
        except Exception as e:
            logger.error(f"Critical Error in Batch {batch_num+1} Phase 1: {e}") # Log critical errors

        # Phase 2: Update references with the stored references for this batch
        logger.info(f"Batch {batch_num+1}: Phase 2 - Updating references...")
        
        # Look up UIDs for references without them
        for (kind, service), objects in objects_by_kind_service.items():
            all_refs = []
            for obj in objects:
                all_refs.extend(obj['references'])
            
            # Only process refs without UIDs but with name/namespace
            refs_missing_uid = [
                ref for ref in all_refs 
                if not ref.get('uid') and ref.get('name') and ref.get('namespace') and ref.get('kind')
            ]
            
            if refs_missing_uid:
                logger.info(f"Looking up UIDs for {len(refs_missing_uid)} references for kind '{kind}' in service '{service or 'default'}'")
                
                # Group references by kind for efficient batch lookup
                refs_by_kind = {}
                for ref in refs_missing_uid:
                    ref_kind = ref.get('kind')
                    if ref_kind not in refs_by_kind:
                        refs_by_kind[ref_kind] = []
                    refs_by_kind[ref_kind].append(ref)
                
                # Look up UIDs in batches by kind
                session = Session()
                try:
                    for ref_kind, refs in refs_by_kind.items():
                        # Skip if no references to look up
                        if not refs:
                            continue
                            
                        # Special case for namespace - use common table
                        if ref_kind.lower() == 'namespace':
                            table = find_table_by_kind_service('namespace', None)
                        else:
                            # Always use the current service's table for lookups
                            table = find_table_by_kind_service(ref_kind, service)
                        
                        if table is None:
                            logger.warning(f"Table not found for '{ref_kind}' in service '{service or 'default'}'")
                            continue
                        
                        # Build lookup conditions
                        conditions = []
                        params = {}
                        for i, ref in enumerate(refs):
                            name = ref.get('name')
                            namespace = ref.get('namespace')
                            tenant = ref.get('tenant')
                            
                            # Create parameter names
                            name_param = f"name_{i}"
                            ns_param = f"ns_{i}"
                            tenant_param = f"tenant_{i}"
                            
                            # Build condition
                            condition = f"(name = :{name_param} AND namespace = :{ns_param}"
                            params[name_param] = name
                            params[ns_param] = namespace
                            
                            if tenant:
                                condition += f" AND tenant = :{tenant_param}"
                                params[tenant_param] = tenant
                            
                            condition += ")"
                            conditions.append(condition)
                        
                        if not conditions:
                            continue
                        
                        # Execute query
                        query = f"""
                            SELECT name, namespace, tenant, uid 
                            FROM {table.name} 
                            WHERE {' OR '.join(conditions)}
                        """
                        
                        try:
                            results = session.execute(text(query), params).fetchall()
                            
                            # Create mapping for quick lookup
                            uid_map = {}
                            for row in results:
                                name = row[0]
                                namespace = row[1]
                                tenant = row[2]
                                uid = row[3]
                                
                                # Use tenant as part of key if available
                                key = (name, namespace, tenant) if tenant else (name, namespace, None)
                                uid_map[key] = uid
                            
                            # Update references with UIDs
                            for ref in refs:
                                name = ref.get('name')
                                namespace = ref.get('namespace')
                                tenant = ref.get('tenant')
                                
                                # Try with tenant first, then without
                                key_with_tenant = (name, namespace, tenant)
                                key_without_tenant = (name, namespace, None)
                                
                                if key_with_tenant in uid_map:
                                    ref['uid'] = uid_map[key_with_tenant]
                                elif key_without_tenant in uid_map:
                                    ref['uid'] = uid_map[key_without_tenant]
                                else:
                                    logger.debug(f"No match found for {ref_kind}/{name}/{namespace} in service '{service or 'default'}'")
                        
                        except Exception as e:
                            logger.error(f"Error looking up UIDs for {ref_kind} in service '{service or 'default'}': {e}")
                            continue
                            
                except Exception as e:
                    logger.error(f"Error in reference lookup: {e}")
                finally:
                    session.close()

        # Update references using the looked-up UIDs
        batch_updated_count = 0

        # Process each object in its own transaction
        for (kind, service), objects in objects_by_kind_service.items():
            # Get or create table with references
            references = list(kind_service_references.get((kind, service), set()))
            
            # Ensure table exists before attempting to update references
            try:
                table = get_or_create_table(kind, service, references)
            except Exception as e:
                 logger.error(f"Failed to get/create table for reference update (kind={kind}, service={service}): {e}")
                 # Skip updating references for these objects if table is problematic
                 continue 

            for obj in objects:
                # New session for each object
                session = Session()
                
                try:
                    uid = obj['uid']
                    name = obj['name']
                    namespace_name = obj['namespace']
                    tenant = obj['tenant']
                    obj_service = obj['service']
                    
                    # Group references by kind
                    refs_by_kind = {}
                    for ref in obj['references']:
                        ref_kind = ref.get('kind')
                        # Ensure the reference kind is valid and expected for this table
                        if ref_kind and ref_kind != kind and ref_kind in kind_service_references.get((kind, service), set()):
                            if ref_kind not in refs_by_kind:
                                refs_by_kind[ref_kind] = []
                            refs_by_kind[ref_kind].append(ref)
                    
                    # Special handling for namespace reference
                    # Make sure every object references its namespace
                    if kind != 'namespace' and 'namespace' in kind_service_references.get((kind, service), set()):
                        # Check if we already have a namespace reference
                        has_namespace_ref = 'namespace' in refs_by_kind and any(
                            ref.get('uid') for ref in refs_by_kind['namespace']
                        )
                        
                        if not has_namespace_ref:
                            # Look up the namespace UID from the cache
                            cache_key = (namespace_name, tenant) if tenant else (namespace_name, None)
                            namespace_uid = namespace_cache.get(cache_key)
                            
                            # Try without tenant if not found
                            if not namespace_uid and tenant:
                                cache_key = (namespace_name, None)
                                namespace_uid = namespace_cache.get(cache_key)
                            
                            if namespace_uid:
                                # Add namespace reference
                                if 'namespace' not in refs_by_kind:
                                    refs_by_kind['namespace'] = []
                                
                                refs_by_kind['namespace'].append({
                                    'kind': 'namespace',
                                    'name': namespace_name,
                                    'namespace': 'system',
                                    'uid': namespace_uid,
                                    'tenant': tenant,
                                    'service': None  # Namespaces use common table with no service
                                })
                            else:
                                logger.warning(f"Could not find UID for namespace {namespace_name} (tenant: {tenant}) for object {uid}")
                    
                    # Prepare updates for each reference kind
                    updates = {}
                    for ref_kind, refs in refs_by_kind.items():
                        ref_uids = []
                        
                        for ref in refs:
                            ref_uid = ref.get('uid')
                            if ref_uid and ref_uid not in ref_uids:
                                ref_uids.append(ref_uid)
                        
                        if ref_uids:
                            # Add the reference UIDs to our updates as a simple array
                            ref_col = f"ref_{ref_kind.lower()}"
                            # Ensure the column exists in the table before trying to update
                            if ref_col in table.c:
                                updates[ref_col] = ref_uids
                            else:
                                logger.warning(f"Reference column '{ref_col}' not found in table '{table.name}' for object {uid}. Skipping update.")

                    # Update the record with references if we have any
                    if updates:
                        try:
                            # Update each reference kind one at a time
                            for ref_col, ref_uids in updates.items():
                                session.execute(
                                    update(table).where(table.c.uid == obj['uid']).values({ref_col: ref_uids})
                                )
                            
                            # Commit immediately after processing this object
                            session.commit()
                            batch_updated_count += 1
                            
                            if batch_updated_count % 100 == 0:
                                logger.info(f"Batch {batch_num+1}: Phase 2 - Updated references for {batch_updated_count} objects")
                        except Exception as e:
                            # Roll back this object's transaction
                            session.rollback()
                            logger.error(f"Error updating references for {kind}/{name} (service: {service or 'default'}, uid: {uid}): {e}")
                            
                            # Track this error
                            error_records[obj['key']] = {
                                "value": obj['value'],
                                "error": f"Reference update error: {str(e)}",
                                "kind": kind,
                                "service": service,
                                "name": name,
                                "uid": uid,
                                "namespace": namespace_name
                            }
                            
                            # Use helper function to save to D2SkippedData
                            save_to_d2_skipped_data(
                                key=obj['key'],
                                kind=kind,
                                service=service,
                                name=name,
                                uid=uid,
                                namespace=namespace_name,
                                tenant=tenant,
                                error_type="reference_update_error",
                                error_message=str(e),
                                size_bytes=obj['size']
                            )
                                
                except Exception as e:
                    # Roll back this object's transaction
                    session.rollback()
                    logger.error(f"Error processing references for {kind}/{name} (service: {service or 'default'}, uid: {uid}): {e}")
                    
                    # Track this error
                    error_records[obj['key']] = {
                        "value": obj['value'],
                        "error": f"Reference processing error: {str(e)}",
                        "kind": kind,
                        "service": service,
                        "name": name,
                        "uid": uid,
                        "namespace": namespace_name
                    }
                    
                    # Use helper function to save to D2SkippedData
                    save_to_d2_skipped_data(
                        key=obj['key'],
                        kind=kind,
                        service=service,
                        name=name,
                        uid=uid,
                        namespace=namespace_name,
                        tenant=tenant,
                        error_type="reference_processing_error",
                        error_message=str(e),
                        size_bytes=obj['size']
                    )
                        
                finally:
                    session.close()
        
        logger.info(f"Batch {batch_num+1}: Phase 2 - Successfully updated references for {batch_updated_count} objects")
        
        # Explicitly clear batch data structures and run garbage collection
        del objects_by_kind_service
        del kind_service_references
        del current_batch
        gc.collect()
        logger.info(f"Batch {batch_num+1}: Memory cleanup complete.")

    # Final Summary Report
    logger.info("="*30 + " Processing Summary " + "="*30)
    logger.info(f"Total items received: {initial_total_items}")
    logger.info(f"Total items successfully processed: {total_processed_items}")
    logger.info(f"Total items skipped: {total_skipped_items}")
    logger.info(f"Items fixed (missing namespace): {total_fixed_namespace_items}")
    logger.info(f"Items fixed (missing name/UID): {total_fixed_name_uid_items}")
    
    if total_skipped_items > 0:
        logger.info("Skipped item breakdown by error type:")
        for error_type, count in skipped_by_error_type.items():
            logger.info(f"  - {error_type}: {count}")
            
    logger.info("="*78) # Match the length of the header line

    # Optionally return summary data
    # return {
    #     "total_received": initial_total_items,
    #     "total_processed": total_processed_items,
    #     "total_skipped": total_skipped_items,
    #     "fixed_namespace": total_fixed_namespace_items,
    #     "fixed_name_uid": total_fixed_name_uid_items,
    #     "skipped_details": dict(skipped_by_error_type)
    # }