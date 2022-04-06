..
   Copyright © 2012-2014 Cask Data, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

:orphan:

.. index::
   single: REST API: Importing & Exporting Cluster Templates

=================================================
REST API: Importing & Exporting Cluster Templates
=================================================

.. include:: /rest/rest-links.rst

The REST APIs allow you to export all Providers, Hardware Types, Image Types, Services, and Cluster Templates created into a JSON Object that can then be imported into another Coopr server.  

.. _entity-export:

Export Template Metadata
========================

To export all entities, make a HTTP GET request to URI:
::

 /export

The response is a JSON Object with keys for providers, hardwaretypes, imagetypes, services, and clustertemplates.  Each key has a JSON array as its value, with each element in the array as the json representation as described in the corresponding sections for providers, hardware types, image types, services, and cluster templates.

HTTP Responses
^^^^^^^^^^^^^^

.. list-table:: 
   :widths: 15 10 
   :header-rows: 1

   * - Status Code
     - Description
   * - 200 (OK)
     - Successfully created
   * - 401 (UNAUTHORIZED)
     - The user is unauthorized and cannot perform an export.

Example
^^^^^^^^
.. code-block:: bash

 $ curl -H 'Coopr-UserID:admin' 
        -H 'Coopr-TenantID:<tenantid>'
        -H 'Coopr-ApiKey:<apikey>'
        http://<server>:<port>/<version>/export
   {
     "providers":[ ... ],
     "hardwaretypes":[ ... ],
     "imagetypes":[ ... ],
     "services":[ ... ],
     "clustertemplates":[ ... ]
   }

.. _entity-import:

Import Template Metadata
========================

To import entities, make a POST HTTP request to URI:
::

 /import

The post body must be a JSON object of the same format as the export result.  It has a key for providers, hardwaretypes, imagetypes, services, and clustertemplates.  The value for each key is a JSON array, with each element in the array as a JSON object representation of the corresponding entity.  

.. note:: Imports will wipe out all existing entities, replacing everything with the entities given in the post body. 

HTTP Responses
^^^^^^^^^^^^^^

.. list-table::
   :widths: 15 10
   :header-rows: 1

   * - Status Code
     - Description
   * - 200 (OK)
     - Successful
   * - 403 (FORBIDDEN)
     - If a non-admin user tries to import entities into the server.

Example
^^^^^^^^
.. code-block:: bash

 $ curl -X POST
        -H 'Coopr-UserID:admin' 
        -H 'Coopr-TenantID:<tenantid>'
        -H 'Coopr-ApiKey:<apikey>'
        -d '{ 
              "providers":[...],
              "imagetypes":[...],
              "hardwaretypes":[...],
              "services":[...],
              "clustertemplates":[...]
            }' http://<server>:<port>/<version>/import

