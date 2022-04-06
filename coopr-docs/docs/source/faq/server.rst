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

.. _faq-server:

.. index::
   single: FAQ: Coopr Server

============================
Coopr Server
============================

.. _faq-server-1:

How many concurrent provisioning jobs can Coopr handle?
------------------------------------------------------------------
Coopr Server is built upon Netty. It's highly asynchronous. We have tested it to handle tens of thousands of concurrent requests.
However, we believe that there is lot of room for improvement. We are committed on improving its performance in the future releases.

.. _faq-server-2:

Can I scale-up or scale-down a cluster?
----------------------------------------
Currently not but it is planned for a future release.

.. _faq-server-3:

Do I have the ability to import and export configurations from one cluster to another?
----------------------------------------------------------------------------------------
Yes, you can import and export the meta data of cluster templates. Please refer to the web service 
section :doc:`here</rest/index>` for more information.

.. _faq-server-4:

Where are the configurations of cluster template and it's metadata stored?
----------------------------------------------------------------------------
Cluster templates and their associated metadata are stored in a database.

.. _faq-server-5:

How do I setup a database for Coopr to use?
------------------------------------------------------
Coopr Server persists runtime information of provisioned clusters in a relational database. 
If you are running Coopr Server in the default mode, it's using the embedded Derby DB for storing all 
the runtime metadata. We don't recommend running an embedded derby DB in production. 
Coopr Server can be configured to connect to standard relational database like MySQL or PostgreSQL
by simple configurations specified in ``coopr-security.xml``.

.. highlight:: xml

Following are the configuration required for enabling Coopr Server to connect to external relational database::

 ...
 <property>
    <name>server.jdbc.driver</name>
    <value>com.mysql.jdbc.Driver</value>
    <description>specifies db driver</description>
  </property>
  <property>
    <name>server.jdbc.connection.string</name>
    <value>jdbc:mysql://127.0.0.1:3306/coopr?useLegacyDatetimeCode=false</value>
    <description>specifies how to connect to mysql</description>
  </property>
  <property>
    <name>server.db.user</name>
    <value>coopr</value>
    <description>mysql user</description>
  </property>
  <property>
    <name>server.db.password</name>
    <value>coopring</value>
    <description>mysql user password</description>
  </property>
  ...

.. _faq-server-6:

Is node pooling supported?
----------------------------
Currently not but it is planned for a future release. 

.. _faq-server-7:

What is node pooling?
-----------------------
Node pooling is an advanced feature that allows the clusters to be provisioned instantaneously from a pool
of pre-provisioned nodes. Pre-provisioning includes creation and installation of software components. 
Steps for configuring the cluster and its node are done once the user has requested a cluster to be materialized. 
Administrators will have the ability to enable this feature on a template by template basis. 
Node pooling increases the overall experience of the user for provisioning clusters.

.. _faq-server-8:

Can I run multiple servers concurrently for HA?
-----------------------------------------------
Yes, servers can be configured with the same zookeeper quorum. Both servers will respond to REST calls. Internally,
leader election is used so that only one server is performing task coordination and solving cluster layouts.
Multiple clusters of servers can also be run concurrently if properly configured to ensure that ids across clusters
will not conflict. Please see the BCP documentation :doc:`here</guide/bcp/index>` for more details. 

.. _faq-server-9:

Can I look at the plan before the cluster is being provisioned?
-----------------------------------------------------------------
Currently, we don't have the ability inspect the plan and cluster layout before 
the cluster provisioning is initiated. Cluster layout and plan of execution are 
available once the provisioning cycle has been initiated. 

.. _faq-server-10:

Is there a way to plugin my own planner or layout solver?
-----------------------------------------------------------
Unfortunately, it is not available in this release. The next release will support the ability to plugin your 
very own layout solver.

.. _faq-server-11:

Is there anyway to inspect the plan for cluster being provisioned?
--------------------------------------------------------------------
There is web service endpoint for retrieving the plan for the cluster being provisioned. The plan includes actions
that are executed on the node. Actions are divided into stages. An action in each stage can be executed in parallel.
Coopr Server implements a distributed barrier at each stage ensuring that the planned stage actions are all completed
before proceeding to the next stage. This ensures the actions are executed in the right dependency order.

.. highlight:: console

Following is an example web service call along with the output returned from the Coopr Server provisioning a web server
on a single node::

  $ curl -H 'Coopr-UserID:<user id>' http://<server>:<port>/v2/clusters/<cluster-id>/plans
  $ [{
        "action": "SOLVE_LAYOUT",
        "clusterId": "00000071",
        "currentStage": 0,
        "id": "00000071-001",
        "stages": []
    },{
        "action": "CLUSTER_CREATE",
        "clusterId": "00000071",
        "currentStage": 7,
        "id": "00000071-002",
        "stages": [
            [
                {
                    "id": "00000071-002-001",
                    "nodeId": "17f87422-56d5-4591-9461-5ea02e5d4c42",
                    "service": "",
                    "taskName": "CREATE"
                }
            ],
            [
                {
                    "id": "00000071-002-002",
                    "nodeId": "17f87422-56d5-4591-9461-5ea02e5d4c42",
                    "service": "",
                    "taskName": "CONFIRM"
                }
            ],
            [
                {
                    "id": "00000071-002-003",
                    "nodeId": "17f87422-56d5-4591-9461-5ea02e5d4c42",
                    "service": "",
                    "taskName": "BOOTSTRAP"
                }
            ],
            [
                {
                    "id": "00000071-002-004",
                    "nodeId": "17f87422-56d5-4591-9461-5ea02e5d4c42",
                    "service": "apache-httpd",
                    "taskName": "INSTALL"
                }
            ],
            [
                {
                    "id": "00000071-002-005",
                    "nodeId": "17f87422-56d5-4591-9461-5ea02e5d4c42",
                    "service": "firewall",
                    "taskName": "CONFIGURE"
                }
            ],
            [
                {
                    "id": "00000071-002-007",
                    "nodeId": "17f87422-56d5-4591-9461-5ea02e5d4c42",
                    "service": "hosts",
                    "taskName": "CONFIGURE"
                }
            ],
            [
                {
                    "id": "00000071-002-006",
                    "nodeId": "17f87422-56d5-4591-9461-5ea02e5d4c42",
                    "service": "apache-httpd",
                    "taskName": "CONFIGURE"
                }
            ],
            [
                {
                    "id": "00000071-002-008",
                    "nodeId": "17f87422-56d5-4591-9461-5ea02e5d4c42",
                    "service": "apache-httpd",
                    "taskName": "START"
                }
            ]
        ]
    },
  ]

.. highlight:: java
