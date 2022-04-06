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

==================================
Multi-Datacenter High Availability
==================================

When running across multiple datacenters, Coopr can be configured to be resilient to datacenter failures. This document describes the recommended configuration
for setting up Coopr for HA across multiple datacenters. Together with :doc:`Datacenter High Availability <data-center-bcp>`, this setup provides for a comprehensive plan for Coopr HA.

In this setup, Coopr runs in active mode in all datacenters (Hot-Hot). In case of a datacenter failure, traffic from the failed datacenter will be automatically routed to other datacenters by the load balancer. This ensures that service is not affected on a datacenter failure.

A couple of things need to be considered when configuring Coopr to run across multiple datacenters for HA-

* As discussed in the previous section, all components of Coopr, except for database, either deal with local data or are stateless. The most important part of the HA setup is to share the data across datacenters in a consistent manner. HA configuration setup for multi-datacenter mostly depends on how the database is setup as discussed in the next sections.
* Since Coopr Servers across all datacenters run in Hot-Hot mode, we have to make sure that they do not conflict while creating cluster IDs. The ID space needs to be partitioned amongst the Coopr Servers. This can be done using ``server.ids.start.num`` and ``server.ids.increment.by`` server config parameters. For more information on the config parameters see :doc:`Server Configuration </installation/server-config>` section. Also note that Coopr Servers in a datacenter can share the same ID space.



We discuss two possible multi-datacenter HA configurations for Coopr in the next sections.
Note that the second option - HA with Custom Replication, is still work in progress. 

.. toctree::
   :maxdepth: 2

   option1/index
   option4/index
