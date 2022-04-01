#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup fence_kdump listener plugin."""


from otopi import util

from . import config


@util.export
def createPlugins(context):
    config.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
