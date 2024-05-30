# nuxeo-templaterendering-utils

 
## Description
This plugin contains a utility that adds feature(s) to the [Nuxeo template Rendering](https://doc.nuxeo.com/nxdoc/template-rendering-addon/) addon: It adds an object to the Template Processor Component, `labsAutomation` (just like it already has the `functions`or `Fn` object - cf. the documentation). 

This object allows for running an automation chain when rendering the template. It is then possible to return a value based on more or less complex business logic. The main goal is to avoid building complex freemarker expressions and move these to Automation. Typically, `#if`, several concatenations, custom formating, etc.

> [!IMPORTANT]
> Please read *Installation Warning* below.


### Syntax

You insert the following expression (see the doc on how to insert in a Word template. Basically, insert a MAILMERGE field):

`${labsAutomation.run("chainID"[, "parameter"])}`)

* `chainId`
  * String, required
  * The ID of the chain to run
  * The input of the chain is the document on which the template rendition was called.
* `parameter`
  * String, optional
  * A parameter passed to the chain in the `automationTemplateParam` context variable
  * It can be accessed from the chain:
    * Using `@{automationTemplateParam}` in regular Automation,
    * or `ctx.automationTemplateParam` in JavaScript Automation.

The chain can return a string or a JS object, or a Java Object, as long as the return value can be handled by the template processor (freemarker, most of the time)

> [!IMPORTANT]
> This sandbox plugin has not been tested with all and every possible return types.
> Some return types will obviously not work, like returning an image in a blob for example


### Examples

> [!NOTE]
> * In all the examples below, we just show the expression, not the whole MAILMERGE value as displayed in Word
> * Also, we show the raw result, of course, without any formating (bold, different fonts, etc.) 


#### Return concatenation of values, using conditions
* The `MyChain` automation script:

```
function(run(input, params) {
  var result = input.title;
  if(input["dc:format"]) {
    result += ", " + input["dc:format"];
  }
  if(input["contract:amount"] > 10000) {
    result += ", WARNING: Big contract"
  }

  return result;
})
```

* In the Word template, insert `${labsAutomation.run("javascript.MyChain")}`:

```
. . . some text . . .
Summary: ${labsAutomation.run("javascript.MyChain")}.
. . . more text . . .
```

* The result would be something like:

```
. . . some text . . .
Summary: The title, WARNING: Big contract.
. . . more text . . .

```

#### Return an array of related doc. titles, loop in the Word document

In this example, we show a script that queries for related documents. This would be complex to do from inside the template.

* The `MyChain` automation script:

```
function(run(input, params) {
  // input["relations:docs"] is a string multivalued, holding doc IDs.
  var docs = input["relations:docs"];
  var result = [];
  var i;

  for(i = 0; i < docs.size(); i++) {
    var relatedDoc = Repository.GetDocument(null, {"value": docs[i]});
    result.push(relatedDoc.title)
  }

  return result;
})
```

* In the Word template, we loop (again, showing only the expression to insert, not the MAILMERGE)


```
. . . some text . . .
Related documents:
[#list labsAutomation.run("javascript.MyChain") as item]
  * ${item}
[/#list]
. . . more text . . .
```

* The result would be something like:

```
. . . some text . . .
Related documents:
  * Contract 1234
  * meeting-recording.mp4
  * Default Policy.pdf
. . . more text . . .
```


#### Return an array of objects. Loop in the Word document and display the objects

* The `MyChain` automation script:

```
function(run(input, params) {
  // input["relations:docs"] is a string multivalued, holding doc IDs.
  var docs = input["relations:docs"];
  var result = [];
  var i;

  for(i = 0; i < docs.size(); i++) {
    var relatedDoc = Repository.GetDocument(null, {"value": docs[i]});
    result.push({
      "type": relatedDoc.type,
      "title": relatedDoc.title
    })
  }

  return result;
})
```


* In the Word template, we loop (showing only the expression to insert, not the MAILMERGE)


```
. . . some text . . .
Related documents:
[#list labsAutomation.run("javascript.MyChain") as item]
  * ${item.type}: ${item.title}
[/#list]
. . . more text . . .
```

* The result would be something like:

```
. . . some text . . .
Related documents:
  * Contract: Contract 1234
  * Video: meeting-recording.mp4
  * File: Default Policy.pdf
. . . more text . . .
```



## Installation Warning
Important: nuxeo-template-rendering plugin must be installed, it does not come as a dependency. This is because the same plugin can be used with both LTS2021 and LTS2023, and we want to avoid deploying the wrong version. So, you must make sur nuxeo-templare-rendering is deployed.

If nuxeo-template-rendering is not installed, you server will not start:

```
======================================================================
= Component Loading Status: Pending: 0 / Missing: 1 / Unstarted: 0 / Total: 587
  * service:nuxeo.labs.utils.template-processor references missing [target=org.nuxeo.template.service.TemplateProcessorComponent;point=contextExtension]
======================================================================
ERROR [main] [org.nuxeo.runtime.deployment.NuxeoStarter] Exception during startup
. . .
INFO  [main] [org.nuxeo.osgi.application.loader.FrameworkLoader] Nuxeo Platform is Trying to Shut Down within 1m00s
INFO  [main] [org.nuxeo.osgi.application.loader.FrameworkLoader] Nuxeo Platform has Shut Down in 0m02s
```



## Support

**These features are not part of the Nuxeo Production platform.**

These solutions are provided for inspiration and we encourage customers to use them as code samples and learning resources.

This is a moving project (no API maintenance, no deprecation process, etc.) If any of these solutions are found to be useful for the Nuxeo Platform in general, they will be integrated directly into platform, not maintained here.


## Licensing

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

For handling .icl files, this plugin uses a java library, [biweekly](https://github.com/mangstadt/biweekly). Its license is business friendly (use it as you want, with the license disclaimer, see below).


## About Nuxeo

[Nuxeo](https://hyland.com/nuxeo), developer of the leading Content Services Platform, is reinventing enterprise content management (ECM) and digital asset management (DAM). Nuxeo is fundamentally changing how people work with data and content to realize new value from digital information. Its cloud-native platform has been deployed by large enterprises, mid-sized businesses and government agencies worldwide. Customers like Verizon, Electronic Arts, ABN Amro, and the Department of Defense have used Nuxeo's technology to transform the way they do business. Founded in 2008, the company is based in New York with offices across the United States, Europe, and Asia.

