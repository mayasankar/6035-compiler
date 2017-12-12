def divByTwo(i):
	if i>=0 or i % 1024 == 0:
		return i // 1024
	else:
		return i // 1024 + 1


image = [5*i for i in range(20)]
sharp = [i for i in range(20)]

for c in range(5):
   for r in range(5):
       image[r*2+c*2+1] = divByTwo(image[r*2+c*2+1] * (1024-4*sharp[c+r*2]))
       if image[r*2+c*2+1] >= 2:
           image[r*2+c*2+1] = 1

for i in range(20):
   print(i, image[i])
