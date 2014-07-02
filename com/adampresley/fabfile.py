from __future__ import with_statement

import os
import fabric
import subprocess

from fabric.api import *
from fabric.api import task
from fabric.colors import red
from fabric.colors import green
from fabric.colors import yellow


CLASSPATH = (
	"C:/jdk1.7.0_11/lib/rt.jar",
	"C:/robocode/libs/robocode.jar",
	"C:/robocode/robots",
)

OPTIONS = (
	"-deprecation",
	"-g",
	"-source",
	"1.6",
	"-encoding",
	"UTF-8"
)

FILES = (
	"./Enemy.java",
	"./IntelOfficer.java",
	"./AdamP.java",
)



###############################################################################
# SECTION: Tasks
###############################################################################

@task
def compile():
	print("")
	print(green("Compiling robot..."))

	for f in FILES:
		cmd = "javac %s %s -classpath %s" % (f, " ".join(OPTIONS), ";".join(CLASSPATH))
		local(cmd)
