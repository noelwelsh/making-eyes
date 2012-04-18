---
layout: page
title: BlueEyes' Concurrency Model
---

The main construct for concurrency in BlueEyes is the `Future`. A Future represents a computation that will finish at some point in ... the future. BlueEyes uses [Akka's Future implementation](http://doc.akka.io/docs/akka/2.0.1/scala/futures.html). It is used pervasively in BlueEyes, so we must become familiar with it to write any sizable system.


## Using Futures

Methods on futures `map`, `flatMap`

## Creating Futures

Creating futures `Promise`

## Actors
