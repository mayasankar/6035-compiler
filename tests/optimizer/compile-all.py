#!/usr/bin/env python
from os import system
import os

ori_dir = os.path.dirname(os.path.realpath(__file__));
run_sh = ori_dir + "/../../run.sh";
input_dir = ori_dir + "/input";
nopt_output_dir = ori_dir + "/__asmnopt";
opt_output_dir = ori_dir + "/__asmopt";
system("rm -rf " + nopt_output_dir);
system("rm -rf " + opt_output_dir);
system("mkdir " + nopt_output_dir);
system("mkdir " + opt_output_dir);
for root, dirs, files in os.walk(input_dir):
    for fname in files:
        if fname.find(".dcf") != -1:
            print "Processing: " + fname;
            sname = fname[0:len(fname) - 4] + ".s";
            ret = system(run_sh + " --target=assembly " + root + "/" + fname + " -o " + nopt_output_dir + "/" + sname);
            if ret != 0:
                print "Failed to generate non-optimized code for " + fname;
            ret = system(run_sh + " --opt=all --target=assembly " + root + "/" + fname + " -o " + opt_output_dir + "/" + sname);
            if ret != 0:
                print "Failed to generate optimized code for " + fname;
