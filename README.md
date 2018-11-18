templateEngine
==============

java template enginge for web projects.

It's main focus is to keep the templates (html) and the source code (java) stricted separate. 

Key features:

* ___Auto-Documentation___: java programmer throws a variable into the engine and the HTML coder does have a documentation (dictionary) for all existing variables
* ___Internal Server-Side Template-Interpreter___: an internal server-side interpreter included into the template engine enables even simple java codes to be placed into the templates (so the java programmer don't have to deal with formating issues for single variables!)
* ___HTML Coder don't need access___: templates could be located outside (ftp or svn), so the programmer don't have to give the html coder full access to the sourcecode
* ___HTML Attribute Keywords___: so the HTML-Coder don't have to have XML or Java or any special knowledge but HTML


## core concept: HTML+CSS artists freedom
hydTemplate is created specially for HTML+CSS artists, forced to work with
programmers, working on the server side.

The main problem: the different mindsets of programmers and HTML artists.

This template engine is focusing on both sides.

The programmer can focus on software writing and filling the template engine 
with export data.

The designer can focus on his HTML+CSS code an could use standard HTML format
to write his templates, which will ( in general ) will be rendered nicely as
HTML, even as a template. 

### HTML attribute enrichment
hydTemplate is doing this by enriching the standard tag style 
```<title>Page 1</title>``` with new attributes like 
```<title hyd:content="pagename">Page 1</title>```.

Visually, this tag enrichment will have no impact of the browser view, so that
you can simply write your HTML code and check it without any hesitation. Later,
the engine will remove the ```hyd:content``` tag and fill the content of the 
innerHTML of title with the replacement.

### Activation in HTML
To have the tag attribution enrichment activated, you should place this tag on 
top of every HTML page you create:

```
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:hyd="http://xml.hayde.eu/namespaces/hyd"> 
```


## Attributes

### hyd:replace
replaces the complete tag with the content of the field requested.

```username = "Mustermann"```

Template:

```
You are <span hyd:replace="username">Username</span>, aren't you?
```

Rendered:

```
You are Mustermann, aren't you?
```

### hyd:content
changes the content of the innerHTML of a/the tag.

```username = "Mustermann"```

Template:

```
You are <b hyd:content="username">Username</b>, aren't you?
```

Rendered:

```
You are <b>Mustermann</b>, aren't you?
```

### hyd:include
includes another template into this tag and removes the tag in total

```
header.html:
============

<meta charset="utf-8"/>
<meta http-equiv="expires" content="0"/>
```


template:

```
<head>
<span hyd:include="header.html">header to be replaced</span>
</head>
```


rendered:

```
<head>
<meta charset="utf-8"/>
<meta http-equiv="expires" content="0">
</head>
```

### hyd:condition
Checks for a condition and add's the tag if the condition is qualified, or
deletes the complete tag.

```gender = 'male'```

Template:

```
<span hyd:condition="gender=='male'">Mr.</span>
<span hyd:condition="gender=='female'">Mrs.</span>
<span hyd:condition="gender!='female' && gender !='male'">??</span>
```

rendered:

```
<span>Mr.</span>


```

### hyd:removeTag
if this attribute is set, it does remove the complete tag and leaves the content
alone.

let's take the example of condition and change the template:

```
<span> hyd:condition="gender=='male'" hyd:removeTag="">Mr.</span>
```

rendered:

```
Mr.
```

### hyd:attributes
this allows to add new attributes to the current tag.

```
image_link = "http://www.no.com/image_123.jpg";
description = "hier is a sample picture to read";
html_id = null;
```

Template:

```
<a hyd:attributes="href:image_link; alt:description, id:html_id[noval]"/>
```

rendered:

```
<a href="http://www.no.com/image_123.jpg" alt="hier is a sample picture to read"/>

```

