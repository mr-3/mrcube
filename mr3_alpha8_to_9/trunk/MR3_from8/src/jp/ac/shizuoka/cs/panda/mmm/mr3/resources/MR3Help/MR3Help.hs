<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
          "http://java.sun.com/products/javahelp/helpset_1_0.dtd">

<helpset version="2.0">

   <!-- title -->
   <title>MR^3 Help</title>

   <!-- maps -->
   <maps>
      <homeID>intro</homeID>
      <mapref location="Map.jhm" />
   </maps>

   <!-- views -->
   <view>
      <name>MR3HelpTOC</name>
      <label>Table Of Contents</label>
      <type>javax.help.TOCView</type>
      <data>MR3HelpTOC.xml</data>
      <GUI isMaximize="true" indexAll="500" index="200" />
   </view>
<!--
   <view>
      <name>Index</name>
      <label>Index</label>
      <type>javax.help.IndexView</type>
      <data>MR3HelpIndex.xml</data>
      <GUI isMaximize="true" indexAll="500" index="200" />
   </view>
-->
   <view>
      <name>Search</name>
      <label>Search</label>
      <type>javax.help.SearchView</type>
      <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch</data>
      <jhindexer additionalParameter="" fileFolderArray="help" searchLogFile="" />
      <GUI isMaximize="true" indexAll="500" index="200" />
   </view>

   <!-- presentations -->
   <presentation xml:lang="ja" default="true">
      <name>main window</name>
      <size width="800" height="600" />
      <location x="0" y="0" />
   </presentation>

</helpset>
