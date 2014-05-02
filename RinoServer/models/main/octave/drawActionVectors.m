
clear; close all;
%~ clc

data = load('full_train');
labels = data(:, 1);
vectors = data(:, 2:end);
vector_len = size(vectors, 2);

%~ size(vectors)
n = 8;
%~ n = 1;

for i = 0:(n-1)
	action_v = vectors(labels(:,1) == i,:);
	all_other_v = vectors(labels(:,1) != i,:);

	action_mean = sum(action_v) / size(action_v,1);
	all_other_mean = sum(all_other_v) / size(all_other_v,1);

	compare_mean = [action_mean', all_other_mean'];
	diff_mean = action_mean - all_other_mean;
	

	figure(1, "visible", "off", 'Position', [0,0,720,900]);
	subplot (n, 1, i+1);
	h1 = bar([1:vector_len], compare_mean);
	set (h1(1), "facecolor", "cyan");
	set (h1(2), "facecolor", [0.2 0.2 0.2]);
	max_y = max(max(compare_mean));
	axis ([0, vector_len+1, 0, max_y + 0.1]);
	
	figure(2, "visible", "off", 'Position', [0,0,720,900]);
	subplot (n, 1, i+1);
	h2 = bar([1:vector_len], diff_mean);
	set (h2(1), "facecolor", [0.2 0.2 0.2]);
	max_y = max(max(diff_mean));
	min_y = min(min(diff_mean));
	axis ([0, vector_len+1, min_y - 0.1, max_y + 0.1]);
end;

%~ pause;
print(1, "compare8.png", "-dpng", "-F:6", "-S720,900");
print(2, "diff8.png", "-dpng", "-F:6", "-S720,900");


