#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup pki plugin."""


from otopi import util

from . import ca
from . import ssh


@util.export
def createPlugins(context):
    ca.Plugin(context=context)
    ssh.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
