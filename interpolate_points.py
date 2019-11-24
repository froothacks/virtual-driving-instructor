# def interpolateY(x0, y0, x1, y1, x):
# 	return (())
import math

def length(p1, p2):
	return math.hypot(p1[0] - p2[0], p1[1] - p2[1])

raw_points = [[[43.0094622, -81.2727524],[43.007773, -81.273326]],
[[[43.007773, -81.273326]],[43.006640, -81.273462]],
[[43.006640, -81.273462],[43.006162, -81.270554]],
[[43.006162, -81.270554], [43.008008, -81.269925]],
[[43.008008, -81.269925], [43.007556, -81.266778]],
[[43.007556, -81.266778], [43.009066, -81.261326]],
[[43.009066, -81.261326],[43.009789, -81.262096]],
[[43.009789, -81.262096], [43.011538, -81.258545]],
[[43.011538, -81.258545], [43.012140, -81.256093]],
[[43.012140, -81.256093], [43.010977, -81.255427]],
[[43.010977, -81.255427], [43.010382, -81.257365]]]
 
points = [raw_points[0][0]] + [x[1] for x in raw_points]
# print(points)
#[(1, 1), (1, 3), (2, 3), (2, 10)]

def total_length(points):
	if len(points) <= 1:
		return 0
	total = 0
	for i in range(0, len(points) - 1):
		total += length(points[i], points[i+1])
	return total

def interpolate(p1, p2, numPoints):
	frac = 1/numPoints
	points = [p1]
	while frac < 1:
		points.append([p1[0]*(1-frac)+p2[0]*frac, p1[1]*(1-frac)+p2[1]*frac])
		frac += 1/numPoints
	# points.append(p2)
	# print(points)
	return points

totalPoints = 3000
t = total_length(points)
allp = []
for i in range(0, len(points) - 1):
	cur = length(points[i], points[i+1])
	allp += interpolate(points[i], points[i+1], totalPoints/(t/cur))
	# print("curnumpoints", totalPoints/(t/cur))
	# print(len(allp))
allp.append(points[-1])
print(allp)