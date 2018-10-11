#!/bin/ksh
# Perform the SSH connection to the provided PC and launch SYSMON tool on it.
# Parameters:
#    1- the name of the PC
# Return:
#    the SYSMON tool execution result
# Display on console:
#    Line 1: the process number to allow killing it if necessary
#    Next lines: the SYSMON tool results

# Display process number
echo $$

# Call SYSMON tool on provided PC
# Login should be sa-indus-a300070797 but it has not yet enough rights for all PCs
ssh aspic3@$1 "sudo /home/ioland/TRANSVERSE/LATEST_TOOLS/sysmon export_boa"
