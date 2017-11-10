#!/usr/bin/env python
from os import system
import time
import os

ori_dir = os.path.dirname(os.path.realpath(__file__));
gcc = "gcc";
opt_input_dir = ori_dir + "/__asmopt";
nopt_input_dir = ori_dir + "/__asmnopt";
lib_dir = ori_dir + "/lib";
system("rm -rf __tmp");
system("mkdir __tmp");
system("cp -rf " + ori_dir + "/data/* " + "__tmp/");
for root, dirs, files in os.walk(nopt_input_dir):
    for fname in files:
        if fname.find(".s") != -1:
            print "Processing: " + fname;
            binarynopt = "__tmp/nopt";
            binaryopt = "__tmp/opt";
            system("rm -rf " + binarynopt);
            system("rm -rf " + binaryopt);
            noptf = root + "/" + fname;
            optf = opt_input_dir + "/" + fname;
            ret = system(gcc + " -L " + lib_dir + " " + noptf + " -o " + binarynopt + " -l6035 -lpthread");
            if ret != 0:
                print "Cannot assemble the unoptimized file: " + noptf;
            ret = system(gcc + " -L " + lib_dir + " " + optf + " -o " + binaryopt + " -l6035 -lpthread");
            if ret != 0:
                print "Cannot assemble the optimized file: " + optf;
            curdir = os.getcwd();
            os.chdir("__tmp");
            system("./nopt | tee nopt.log");
            system("./opt | tee opt.log");
            f = open("nopt.log", "r");
            lines = f.readlines();
            timenopt = int((lines[0].split())[1])
            f.close();
            f = open("opt.log", "r");
            lines = f.readlines();
            timeopt = int((lines[0].split())[1]);
            f.close();
            print "Sppedup: " + str(float(timenopt)/timeopt);
            os.chdir(curdir);
