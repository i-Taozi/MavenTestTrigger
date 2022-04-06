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
   single: REST API: Image

==================
REST API: Image
==================

.. include:: /rest/rest-links.rst

The REST APIs allow you to manage the mapping of image capabilities to "flavors" supported by configured images. An image type maps to multiple flavors as specified by different images. 
Using the image REST APIs, you can manage the image specifications.

Each image configured in the system has a unique name, a short description, and a list of key-value pairs that are required by the backend image provisioner.

.. _image-create:

Create an Image Type
====================

To create a new image type, make a HTTP POST request to URI:
::

 /imagetypes

POST Parameters
^^^^^^^^^^^^^^^^

Required Parameters

.. list-table::
   :widths: 15 10
   :header-rows: 1

   * - Parameter
     - Description
   * - name
     - Specifies the name for the image type. The assigned name must have only
       alphanumeric, dash(-), dot(.), and underscore(_) characters.
   * - description
     - Provides a description for the image type.
   * - providermap
     - Provider map is map of providers and equivalent flavor type for current image type being configured.
       It's currently a map of map.

HTTP Responses
^^^^^^^^^^^^^^

.. list-table:: 
   :widths: 15 10 
   :header-rows: 1

   * - Status Code
     - Description
   * - 200 (OK)
     - Successfully created
   * - 400 (BAD_REQUEST)
     - Bad request, server is unable to process the request or an image with the name already exists 
       in the system.

Example
^^^^^^^^
.. code-block:: bash

 $ curl -X POST 
        -H 'Coopr-UserID:admin' 
        -H 'Coopr-TenantID:<tenantid>'
        -H 'Coopr-ApiKey:<apikey>'
        -d '{"name":"small.example", "description":"Example 1 vCPU, 1 GB RAM, 30+ GB Disk", "providermap": {"openstack": {"flavor":"m1.small"}}}' 
        http://<server>:<port>/<version>/imagetypes

.. _image-retrieve:

View an Image Type
===================

To retrieve details about an image type, make a GET HTTP request to URI:
::

 /imagetypes/{name}

This resource request represents an individual image type for viewing.

HTTP Responses
^^^^^^^^^^^^^^

.. list-table::
   :widths: 15 10
   :header-rows: 1

   * - Status Code
     - Description
   * - 200 (OK)
     - Successful
   * - 404 (NOT FOUND)
     - If the resource requested is not configured or available in system.

Example
^^^^^^^^
.. code-block:: bash

 $ curl -H 'Coopr-UserID:admin' 
        -H 'Coopr-TenantID:<tenantid>'
        -H 'Coopr-ApiKey:<apikey>'
        http://<server>:<port>/<version>/imagetypes/small.example
 $ {"name":"small.example","description":"Example 1 vCPU, 1 GB RAM, 30+ GB Disk","providermap":{"openstack":{"flavor":"m1.small"}}}


.. _image-delete:

Delete an Image Type
====================

To delete an image type, make a DELETE HTTP request to URI:
::

 /imagetypes/{name}

This resource request represents an individual image type for deletion.

HTTP Responses
^^^^^^^^^^^^^^

.. list-table::
   :widths: 15 10
   :header-rows: 1

   * - Status Code
     - Description
   * - 200 (OK)
     - If delete was successful
   * - 404 (NOT FOUND)
     - If the resource requested is not found.

Example
^^^^^^^^
.. code-block:: bash

 $ curl -X DELETE
        -H 'Coopr-UserID:admin' 
        -H 'Coopr-TenantID:<tenantid>'
        -H 'Coopr-ApiKey:<apikey>'
        http://<server>:<port>/<version>/imagetypes/example

.. _image-modify:

Update an Image Type
====================

To update an image type, make a PUT HTTP request to URI:
::

 /imagetypes/{name}

Resource specified above respresents an individual image type request for an update operation.
Currently, the update of image type resource requires complete image type object to be 
in the request body.

PUT Parameters
^^^^^^^^^^^^^^^^

Required Parameters

.. list-table::
   :widths: 15 10
   :header-rows: 1

   * - Parameter
     - Description
   * - name
     - Specifies the name of the image type to be updated. 
   * - description
     - New description or old one for the image type.
   * - providermap
     - Provider map is map of providers and equivalent flavor type for current image type being configured.
       It's currently a map of map.

HTTP Responses
^^^^^^^^^^^^^^

.. list-table::
   :widths: 15 10
   :header-rows: 1

   * - Status Code
     - Description
   * - 200 (OK)
     - If update was successful
   * - 400 (BAD REQUEST)
     - If the resource requested is not found or the fields of the PUT body doesn't specify all the required fields.

Example
^^^^^^^^
.. code-block:: bash

 $ curl -X PUT
        -H 'Coopr-UserID:admin' 
        -H 'Coopr-TenantID:<tenantid>'
        -H 'Coopr-ApiKey:<apikey>'
        -d '{"name":"small.example", "description":"New Example 1 vCPU, 1 GB RAM, 30+ GB Disk", 
             "providermap": {"openstack": {"flavor":"m1.small"},"aws":{"flavor":"aws.small"}}}' 
        http://<server>:<port>/<version>/imagetypes/small.example
 $ curl -H 'Coopr-UserID:admin' 
        -H 'Coopr-TenantID:<tenantid>'
        -H 'Coopr-ApiKey:<apikey>'
        http://<server>:<port>/<version>/imagetypes/small.example
 $ {"name":"small.example","description":"New Example 1 vCPU, 1 GB RAM, 30+ GB Disk",
     "providermap":{"openstack":{"flavor":"m1.small"},"aws":{"flavor":"aws.small"}}}

.. _image-all-list:

List All Image Type
=============================

To list all the image types, make a GET HTTP request to URI:
::

 /imagetypes

HTTP Responses
^^^^^^^^^^^^^^

.. list-table::
   :widths: 15 10
   :header-rows: 1

   * - Status Code
     - Description
   * - 200 (OK)
     - Successful
   * - 400 (BAD REQUEST)
     - If the resource uri is specified incorrectly.

Example
^^^^^^^^
.. code-block:: bash

 $ curl -H 'Coopr-UserID:admin' 
        -H 'Coopr-TenantID:<tenantid>'
        -H 'Coopr-ApiKey:<apikey>'
        http://<server>:<port>/<version>/imagetypes

