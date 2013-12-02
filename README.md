Rino
====

Rino is a project of [intelligent personal assistant](http://en.wikipedia.org/wiki/Intelligent_personal_assistant) for the Russian language.
It's designed for smartphones with Android 4.0 and higher.

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

The entire project consists of three parts:

1. RinoRecognizer - represents an Android application for smartphone devices, utilises RinoLibrary
2. RinoServer - perform a training process of Rino's SVM classificators, utilises RinoLibrary
3. RinoLibrary - consists of classes, that should be identical for both RinoRecognizer and RinoServer
