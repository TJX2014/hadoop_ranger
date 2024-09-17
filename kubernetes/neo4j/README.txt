docker run -d --name neo4j \
    --restart always \
    --publish=7474:7474 --publish=7687:7687 -e NEO4J_AUTH=neo4j/password -e NEO4J_PLUGINS='["apoc"]' \
    -e NEO4J_db_tx__log_rotation_retention__policy=false -e NEO4J_dbms_security_procedures_unrestricted=apoc.*\
    public.ecr.aws/docker/library/neo4j:5.23.0


SHOW INDEXES YIELD name, type, entityType, labelsOrTypes, 
            properties, options WHERE type = 'VECTOR' AND (name = 'stackoverflow' 
            OR (labelsOrTypes[0] = 'Chunk' AND 
            properties[0] = 'embedding')) 
RETURN name, entityType, labelsOrTypes, properties, options

            