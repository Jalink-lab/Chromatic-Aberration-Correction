// Marco to retrieve x,y coordinates of tetraspec beads for chromatic abberation correction.
// Input: 3-channel high-res file with beads
// Output: text file with bead coordinates
//
// author: Bram van den Broek (b.vd.broek@nki.nl), The Netherlands Cancer Institute, 2016

saveSettings();

run("Input/Output...", "jpeg=85 gif=-1 file=.csv use_file copy_column save_column");

var px = 10;			//pixel size of the input image in nm
var noise_maxima = 10000;//noise tolerance for finding bead positions (0-255)
var max_radius_nm = 160;//radius in which beads of all three colors should be present to be taken into account (in nm)
var scale = 1;		//Scaling factor for resulting bead positions image

max_radius = max_radius_nm/px;

if (nImages>0) run("Close All");
print("\\Clear");

path = File.openDialog("Select a 3 channel beads image");
open(path);
original = getTitle();
getDimensions(width, height, channels, slices, frames);
dir = File.getParent(path);


//----------- DETECT BEADS USING 'FIND MAXIMA'

Stack.setChannel(1);
run("Clear Results");
run("Find Maxima...", "noise="+noise_maxima+" output=List");
xr=newArray(nResults);
yr=newArray(nResults);
for(i=0;i<nResults;i++) {
	xr[i]=getResult("X",i);
	yr[i]=getResult("Y",i);
}

Stack.setChannel(2);
run("Clear Results");
run("Find Maxima...", "noise="+noise_maxima+" output=List");
xg=newArray(nResults);
yg=newArray(nResults);
for(i=0;i<nResults;i++) {
	xg[i]=getResult("X",i);
	yg[i]=getResult("Y",i);
}

Stack.setChannel(3);
run("Clear Results");
run("Find Maxima...", "noise="+noise_maxima+" output=List");
xb=newArray(nResults);
yb=newArray(nResults);
for(i=0;i<nResults;i++) {
	xb[i]=getResult("X",i);
	yb[i]=getResult("Y",i);
}


//----------- CLEANUP (ONLY KEEP COORDINATES THAT ARE CLOSE IN ALL THREE COLORS

setForegroundColor(1, 1, 1);
distance_gg=newArray(xg.length);
distance_rr=newArray(xr.length);
distance_bb=newArray(xb.length);
distance_gr=newArray(xr.length);
distance_gb=newArray(xb.length);
distance_rb=newArray(xb.length);
xg2=newArray(nResults);
yg2=newArray(nResults);
xr2=newArray(nResults);
yr2=newArray(nResults);
xb2=newArray(nResults);
yb2=newArray(nResults);

print("detected maxima:");
print("green: "+xg.length);
print("red:   "+xr.length);
print("blue   "+xb.length);

var k=0;		//counter for detected valid beads
var failed = 0;

//start with green, because it is (usually) in the middle
for(i=0;i<xg.length;i++) {
	for(j=0;j<distance_gg.length;j++) distance_gg[j]=((xg[j]-xg[i])*(xg[j]-xg[i]))+((yg[j]-yg[i])*(yg[j]-yg[i]));	//green - green
//	for(j=0;j<distance_rr.length;j++) distance_rr[j]=((xr[j]-xr[i])*(xr[j]-xr[i]))+((yr[j]-yr[i])*(yr[j]-yr[i]));	//red - red
//	for(j=0;j<distance_bb.length;j++) distance_bb[j]=((xb[j]-xb[i])*(xb[j]-xb[i]))+((yb[j]-yb[i])*(yb[j]-yb[i]));	//blue - blue
	for(j=0;j<distance_gr.length;j++) distance_gr[j]=((xr[j]-xg[i])*(xr[j]-xg[i]))+((yr[j]-yg[i])*(yr[j]-yg[i]));	//green - red
	for(j=0;j<distance_gb.length;j++) distance_gb[j]=((xb[j]-xg[i])*(xb[j]-xg[i]))+((yb[j]-yg[i])*(yb[j]-yg[i]));	//green - blue
//	for(j=0;j<distance_rb.length;j++) distance_rb[j]=((xb[j]-xr[i])*(xb[j]-xr[i]))+((yb[j]-yr[i])*(yb[j]-yr[i]));	//red - blue
	
	rank_distance_gg = Array.rankPositions(distance_gg);
	rank_distance_gr = Array.rankPositions(distance_gr);
	rank_distance_gb = Array.rankPositions(distance_gb);
	Array.sort(distance_gg);
	Array.sort(distance_gr);
	Array.sort(distance_gb);

	//If a green bead is too close to another green bead, or if the distance to another color is too large, the coordinates are excluded.
	//Doing the same for red-red and blue-blue requires another round outside the current loop.
	if(distance_gg[1]<=(max_radius*max_radius) || distance_gr[0]>=(max_radius*max_radius) || distance_gb[0]>=(max_radius*max_radius)) {
		failed+=1;
		print(""+failed+" fail with distances "+d2s(sqrt(distance_gg[1]),0)+", "+d2s(sqrt(distance_gr[0]),0)+", "+d2s(sqrt(distance_gb[0]),0));
	}
	else {
		xg2[k]=xg[i];
		yg2[k]=yg[i];
		xr2[k]=xr[rank_distance_gr[0]];
		yr2[k]=yr[rank_distance_gr[0]];
		xb2[k]=xb[rank_distance_gb[0]];
		yb2[k]=yb[rank_distance_gb[0]];
		k++;
	}
}
print("\n"+failed+" out of "+xg.length+" detected green beads are rejected; "+k+" valid beads remaining.");

xg2 = Array.trim(xg2,k);
yg2 = Array.trim(yg2,k);
xr2 = Array.trim(xr2,k);
yr2 = Array.trim(yr2,k);
xb2 = Array.trim(xb2,k);
yb2 = Array.trim(yb2,k);
//----------- SAVING XY COORDINATES OF THE THREE COLORS AND RECONSTRUCT AN IMAGE

newImage("valid bead positions (pixel size "+px*scale+" nm)", "RGB black", width/scale, height/scale, 1);
run("Properties...", "channels=1 slices=1 frames=1 unit=nm pixel_width="+px*scale+" pixel_height="+px*scale+" voxel_depth=1");
run("Clear Results");
setForegroundColor(0xffffff);

for(i=0;i<xg2.length;i++) {
	setResult("x [nm]", i, xr2[i]*px);
	setResult("y [nm]", i, yr2[i]*px);
	setPixel(round(xr2[i]/scale), round(yr2[i]/scale), 0xff0000);
}
selectWindow("Results");
saveAs("results", dir+"\\bead_coords_red.csv");
run("Clear Results");
for(i=0;i<xg2.length;i++) {
	setResult("x [nm]", i, xg2[i]*px);
	setResult("y [nm]", i, yg2[i]*px);
	setPixel(round(xg2[i]/scale), round(yg2[i]/scale), 0x00ff00);
}
selectWindow("Results");
saveAs("results", dir+"\\bead_coords_green.csv");
run("Clear Results");
for(i=0;i<xg2.length;i++) {
	setResult("x [nm]", i, xb2[i]*px);
	setResult("y [nm]", i, yb2[i]*px);
	setPixel(round(xb2[i]/scale), round(yb2[i]/scale), 0x0000ff);
}
selectWindow("Results");
saveAs("results", dir+"\\bead_coords_blue.csv");
//run("Clear Results");
	

print("Saving bead coordinates in "+dir);

restoreSettings();
