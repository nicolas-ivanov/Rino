
function drawVectors(data_model)
%%% get enum types %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%~ actionType = ["any"; "acall"; "asms"; "aemail"; "asearch"; "asite"; "aalarm"; "abalance"; "acancel"];
%~ paramsType = ["other"; "pname"; "pnumber"; "pemail"; "psite"; "ptime"; "quote"];
labelsType = ["other"; "action"; "pname"; "pnumber"; "pemail"; "psite"; "ptime"; "quote"; "qmark"; "prepos"; "change"];


%%% get data sets %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
file_name = strcat(data_model, '/full_train');
data = load(file_name);
labels = data(:, 1);
vectors = data(:, 2:end);
vector_len = size(vectors, 2);


%%% find labels set %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
labels_list = [];
	
for j = 1:length(labels)
	cur_label = labels(j);
	if !ismember(cur_label,labels_list)
		labels_list = [labels_list, cur_label];
	end;
end;

labels_list = sort(labels_list);


%%% set vars %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
n = length(labels_list);
canvas_width = 1320;
canvas_height = 120*n + 180;

figure(1, "visible", "off", 'Position', [0,0,canvas_height,canvas_width]);
	

for i = 1:n

	l = labels_list(i);
	strLabel = labelsType(l+1,:);
	
	%%% compute %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	action_v = vectors(labels(:,1) == l,:);
	all_other_v = vectors(labels(:,1) != l,:);

	action_mean = sum(action_v) / size(action_v,1);
	all_other_mean = sum(all_other_v) / size(all_other_v,1);

	compare_mean = [action_mean', all_other_mean'];
	diff_mean = action_mean - all_other_mean;
	
	
	%%% subplot %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	h_subplot = subplot (n, 1, i);
		
	h_bar = bar([1:vector_len], diff_mean);
	set (h_bar, "facecolor", "k");
	
	max_y = max(max(max(diff_mean)), 1);
	min_y = min(min(diff_mean));
	axis ([0, vector_len + 1, min_y - 0.1, max_y + 0.1]);
	
	ylabel (strcat(strLabel, "\navr. val."));

end;

xlabel ("feature's position in a vector");

%%% print %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

imgSize = strcat("-S",  int2str(canvas_width), ",", int2str(canvas_height));
imgDiffName = strcat(data_model, "/diff_", data_model, ".png");
print(imgDiffName, "-dpng", "-F:6", imgSize);


