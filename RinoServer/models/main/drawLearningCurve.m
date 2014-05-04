function drawLearningCurve(data_model)

file_name = strcat(data_model, '/accuracy');
data = load(file_name);

n = size(data, 1);
train_err = 100 - data(:, 2);
test_err = 100 - data(:, 1);

step = 1 / n;
x = [step : step : 1];

figure(1, "visible", "off");
h1 = plot(x, train_err, "bx", x, test_err, "ro");
title (strcat(regexprep(data_model, "[_/]", ""), " learning curve"));

legend ("train error rate", "test error rate");
xlabel ("relative size of training set");
ylabel ("error %");
set (h1, "linewidth", 2);
axis ([step, 1]);

hold on;
xf = [step : 0.02 : 1];
train_err_f = interp1 (x, train_err, xf, "cubic");
test_err_f = interp1 (x, test_err, xf, "cubic");

h2 = plot(xf, train_err_f, "b", xf, test_err_f, "r");
set (h2, "linewidth", 2);
hold off;

imgName = strcat(data_model, "/lcurve_", data_model, ".png");
print(imgName, "-dpng");


