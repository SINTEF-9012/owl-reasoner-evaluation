import io
import stardog
import pandas as pd
import seaborn as sns

if __name__ == '__main__':
    connection_details = {
        'endpoint': 'http://localhost:5820',
        'username': 'admin',
        'password': 'admin'
    }

    database_name = 'VICODI'

    with stardog.Admin(**connection_details) as admin:
        if database_name in [db.name for db in admin.databases()]:
            admin.database(database_name).drop()
        db = admin.new_database(database_name, options={ "reasoning.type" : "DL", "reasoning.schemas":"http://vicodi.org/ontology=http://vicodi.org/ontology", "reasoning.schema.graphs":"http://vicodi.org/ontology"})

    conn = stardog.Connection(database_name, **connection_details)
    conn.begin()

    conn.add(stardog.content.File('../../ontologies/vicodi_all.owl'), graph_uri="http://vicodi.org/ontology")
    conn.commit()

    if conn.is_consistent():
        print("The data is consitent")
    else:
        print("The data is NOT consitent")

    query = """
     PREFIX : <http://vicodi.org/ontology#>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                SELECT ?s
                WHERE {
                    :Professor rdfs:subClassOf ?s .
                }
    """

    csv_results = conn.select(query, content_type='text/csv', reasoning=True)
    df = pd.read_csv(io.BytesIO(csv_results))
    print(df)

    query = """
     PREFIX : <http://vicodi.org/ontology#>
                SELECT ?s
                WHERE {
                    ?s a :Individual .
                    ?s :hasRole ?r .
                    ?r a :Professor .
                }
    """

    csv_results = conn.select(query, content_type='text/csv', reasoning=True)
    df = pd.read_csv(io.BytesIO(csv_results))
    print(df)

