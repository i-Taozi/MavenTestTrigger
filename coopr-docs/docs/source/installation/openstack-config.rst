..
   Copyright © 2012-2015 Cask Data, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

.. _guide_installation_toplevel:

.. index::
   single: Openstack Configuration

=======================
Openstack Configuration
=======================

* Admin

 * There must be a user in OpenStack which is a member of all projects which will have hosts provisioned by Coopr.

* Networking

 * Instance networks must be routable from Coopr provisioners.
 * If multiple networks, any network named "public" becomes network Coopr Provisioner uses for SSH.
 * If there is only one network, coopr provisioner will use it for SSH.
 * Coopr currently does not support specifying a network.

* SSH Keys

 * OpenStack must be configured with ``libvirt_inject_key true`` for key-based authentication to instances.
 * The key must be present in OpenStack and ``openstack_ssh_key_id`` must be configured in the provider.
 * The private key file must be present on the Coopr provisioner machines in the path specified in ``identity_file`` for the provider.
 * The private key file must be used when using SSH to connect to the instance, rather than a password.

* Passwords

 * OpenStack must be configured with ``libvirt_inject_password true`` for password-based authentication to instances.

* Operating Systems

 * Linux instances are currently supported.
