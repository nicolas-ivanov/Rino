Rino
====

RinoRecognizer is a prototype of [intelligent personal assistant](http://en.wikipedia.org/wiki/Intelligent_personal_assistant) for the Russian language.

Currently there are 7 types of commands supported:

* make a call
* send sms to smb
* send an email to smb
* search smth on the web
* launch some site in browser
* set an alarm for particular time
* check how much money you have on your account

See below the examples of users comands in Russian:

```
«позвони васе»
«отправь смс васе с текстом привет»
«напиши письмо васе с текстом как жизнь»
«найди в яндексе что значит дзен»
«открой википедию»
«поставь на завтра будильник на 7 30»
«покажи баланс
```

The process of commands' analysing is based on [SVM method](http://en.wikipedia.org/wiki/Support_vector_machine),
which is well implemented in [libsmv library](http://www.csie.ntu.edu.tw/~cjlin/libsvm/).
