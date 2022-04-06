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

.. _rest-api-reference:

.. index::
   single: Web Services

=============
Web Services
=============

REST by far is the most efficient and seamless way for disparate and distributed systems to exchange or communicate information. Most 
interactions are characterized as request-response-action-based exchange. And invariably, there is a client (requesting an action or resource)
and the server (providing the response or resource) to complete an exchange between two endpoints over HTTP protocol. In that manner, the REST API, then, allows you to interact
with the system from an administrative and user perspective. You can pretty much do everything that a UI can do using these
REST interfaces. 

Since the API is based on REST principles, it's very easy to write and test applications. You can use your browser to access URLs, 
and use pretty much any http client in any programming language of your choice to interact with the API

Base URL
========

All URLs referenced in the documentation have the following base:
::

 http://<server>:<port>/<version>

The current API version is 'v2'.
The default server port is 55054 but can be reconfigured as described in :doc:`Configuring the server </installation/server-config>`. 
In addition, three headers must be sent to all REST endpoints.  The first is ``Coopr-UserID`` and is used to specify
the id of the user making the request. The second is ``Coopr-ApiKey`` and is used to specify the api key used to
communicate with the server. The third is ``Coopr-TenantID`` and is used to specify the id of the tenant that the
user belongs to.


.. note:: The REST API is served over HTTP. In the near future, the APIs will be served on HTTPS to ensure data privacy, and unencrypted HTTP will not be supported.

Super admin APIs
================

Tenants
-------
  * :ref:`Create a Tenant <tenants-create>`
  * :ref:`View a Tenant <tenants-retrieve>`
  * :ref:`View all Tenants <tenants-all-list>`
  * :ref:`Update a Tenant <tenants-modify>`
  * :ref:`Delete a Tenant <tenants-delete>`

Provisioners
------------
  * :ref:`Get a Provisioner <provisioners-retrieve>`
  * :ref:`Get all Provisioners <provisioners-all-list>`

Metrics
-------
  * :ref:`Queue Metrics <metrics-queues>`


Administration APIs
====================

Provider
------------
  * :ref:`Create a Provider <provider-create>`
  * :ref:`View a Provider <provider-retrieve>`
  * :ref:`Delete a Provider <provider-delete>`
  * :ref:`Update a Provider <provider-modify>`
  * :ref:`View all Providers <provider-all-list>`

Hardware
------------
  * :ref:`Create a Hardware type <hardware-create>`
  * :ref:`View a Hardware type <hardware-retrieve>`
  * :ref:`Delete a Hardware type <hardware-delete>`
  * :ref:`Update a Hardware type <hardware-modify>`
  * :ref:`View all Hardware types <hardware-all-list>`

Image
---------
  * :ref:`Create an Image type <image-create>`
  * :ref:`Retrieve an Image type <image-retrieve>`
  * :ref:`Delete an Image type <image-delete>`
  * :ref:`Update an Image type <image-modify>`
  * :ref:`Retrieve all Image types configured <image-all-list>`

Services
------------
  * :ref:`Add a Service <service-create>`
  * :ref:`Retrieve a Service <service-retrieve>`
  * :ref:`Delete a Service <service-delete>`
  * :ref:`Update a Service <service-modify>`
  * :ref:`List all Services <service-all-list>`

Cluster Templates
--------------------
  * :ref:`Create a Cluster template <template-create>`
  * :ref:`Retrieve a Cluster template <template-retrieve>`
  * :ref:`Delete a Cluster template <template-delete>`
  * :ref:`Update a Cluster template <template-modify>`
  * :ref:`Retrieve all configured Cluster templates <template-all-list>`

Provisioner Plugins
-------------------
  * :ref:`Retrieve all plugin specifications <plugin-spec-all-list>`
  * :ref:`Retrieve a plugin specification <plugin-spec-retrieve>`
  * :ref:`Add a plugin resource <plugin-resource-create>`
  * :ref:`Retrieve all metadata for resources of a specific type <plugin-resourcetype-all-list>`
  * :ref:`Retrieve all metadata for resource of a specific type and name <plugin-resource-all-list>`
  * :ref:`Delete all versions of a resource <plugin-resource-delete>`
  * :ref:`Delete a specific version of a resource <plugin-resource-delete-version>`
  * :ref:`Stage a specific version of a resource <plugin-resource-stage>` 
  * :ref:`Recall a specific version of a resource <plugin-resource-recall>`
  * :ref:`Sync plugins <plugin-sync>`

Import/Export
-----------------
  * :ref:`Export Template Metadata <entity-export>`
  * :ref:`Import Template Metadata <entity-import>`

Metrics
-------
  * :ref:`Queue Metrics <metrics-queues>`

User APIs
=========
The User Web service provides methods that can be used to create, delete and manage clusters. 

Clusters
------------
  * :ref:`Create a Cluster <cluster-create>`
  * :ref:`Get all Clusters <cluster-retrieve-all>`
  * :ref:`Get Cluster Details <cluster-details>`
  * :ref:`Delete a Cluster <cluster-delete>`
  * :ref:`Get Cluster Status <cluster-status>`
  * :ref:`Get a Cluster Action Plan <cluster-plan>`
  * :ref:`Get Cluster Configuration <cluster-get-config>`
  * :ref:`Update Cluster Configuration <cluster-update-config>`
  * :ref:`Get Cluster Services <cluster-get-services>`
  * :ref:`Add Services to a Cluster <cluster-add-services>`
  * :ref:`Stop Services on a Cluster <cluster-stop-services>`
  * :ref:`Start Services on a Cluster <cluster-start-services>`
  * :ref:`Restart Services on a Cluster <cluster-restart-services>`
  * :ref:`Sync Cluster Template to Current Version <cluster-sync-template>`
  * :ref:`Extend the Lease on a Cluster <cluster-extend-lease>`

RPC Calls
=========
In addition to the standard REST endpoints, a few RPC functions are available to obtain cluster information. 

RPC
---
  * :ref:`Getting Properties of Nodes in a Cluster <rpc-properties>`

About REST (REpresentational State Transfer)
===============================================

We designed the API in a very RESTful way, so that your consumption of it is simple and straightforward. 

From Wikipedia:

REST's proponents argue that the Web's scalability and growth are a direct result of a few key design principles:

  * Application state and functionality are divided into resources
  * Every resource is uniquely addressable using a universal syntax for use in hypermedia links
  * All resources share a uniform interface for the transfer of state between client and resource, consisting of
 
   * A constrained set of well-defined operations
   * A constrained set of content types, optionally supporting code on demand

  * A protocol which is:

   * Client-server
   * Stateless
   * Cacheable
   * Layered

REST's client/server separation of concerns simplifies component implementation, reduces the complexity of connector 
semantics, improves the effectiveness of performance tuning, and increases the scalability of pure server components. 
Layered system constraints allow intermediaries-proxies, gateways, and firewalls-to be introduced at various points 
in the communication without changing the interfaces between components, thus allowing them to assist in communication 
translation or improve performance via large-scale, shared caching.

REST enables intermediate processing by constraining messages to be self-descriptive: interaction is stateless between 
requests, standard methods and media types are used to indicate semantics and exchange information, and responses explicitly 
indicate cacheability.

If you're looking for more information about RESTful web services, the O'Reilly RESTful Web Services book is excellent.
