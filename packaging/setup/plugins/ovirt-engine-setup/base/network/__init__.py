#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup network plugin."""

from otopi import util

from . import firewall_manager


@util.export
def createPlugins(context):
    firewall_manager.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
