golden = "tests/derby/output/golden.ppm"
output = "output.ppm"

gold_file = open(golden, "r")
out_file = open(output, "r")
for i in range(3):
	gold_file.readline()
	out_file.readline()
gold_line = gold_file.readline()
out_line = out_file.readline()

gold_arr = gold_line.split(" ")
out_arr = out_line.split(" ")
count = 0

for i in range(len(gold_arr)):
	if gold_arr[i] != out_arr[i]:
		count += 1
		if count < 100:
			print(int(gold_arr[i]) - int(out_arr[i]))
			print(gold_arr[i], out_arr[i])

print(str(count) + " mistakes in " + str(len(gold_arr)) + " pixels\n")
print(str(count * 1.0/len(gold_arr)))

