/**
 * This module provides an API to use in the Java code for controlling the text editor.
 */

 var Editor = {};

 Editor.applyBoldStyle = function() {
     document.execCommand('bold', false, null);
 }

 Editor.applyItalicStyle = function() {
     document.execCommand('italic', false, null);
 }

 Editor.applyStrikeThroughStyle = function() {
      document.execCommand('strikeThrough', false, null);
  }

 Editor.applyUnderlineStyle = function() {
     document.execCommand('underline', false, null);
 }

 Editor.applyBulletListStyle = function() {
    document.execCommand('InsertUnorderedList', false, null);
 }

 Editor.applyNumberListStyle = function() {
    document.execCommand('InsertOrderedList', false, null);
 }

 Editor.undo = function() {
    document.execCommand('undo', false, null);
    return getHtml();
 }

 Editor.redo = function() {
     document.execCommand('redo', false, null);
     return getHtml();
  }

 Editor.initEditorWithContent = function(contentHtml) {
    document.getElementById('editor').innerHTML =
        decodeURIComponent(contentHtml.replace(/\+/g, '%20'));
    return getHtml();
 }

 Editor.setHeading = function(heading) {
   document.execCommand('formatBlock', false, '<h'+heading+'>');
 }

 Editor.setDirection = function(direction) {
    var curDiv = getFocusedDiv();
    if (curDiv == null || curDiv.dir === direction) {
      return;
    }
    var curDivContentHtml = curDiv.innerHTML.trim();
    if (curDiv.id !== "editor") {
        curDiv.dir = direction;
        return;
    }
    var content = getHtml().trim();
    if (content == null || content === '') {
      createNewDivElement(direction);
    } else {
      curDiv.innerHTML = '';
      createNewDivElement(direction, content);
    }
 }

 Editor.numberOfImages = 0;
 Editor.imageIdPrefix = '';
 Editor.insertImage = function(imagePath, imageIdPrefix, alt, width, height) {
    var imageElement = getImageElement(imagePath, imageIdPrefix, alt, width, height);
    document.execCommand('insertHTML', false, imageElement);
    Editor.numberOfImages += 1;
    Editor.imageIdPrefix = imageIdPrefix;
    return getHtml();
 }

 Editor.getFeatureImageSource = function() {
    regex = new RegExp(/id="noteImage./gi);
    matches = getHtml().match(regex);
    if (matches == null) {
        return null;
    }
    var imageElementId = matches[0].split("id=\"").filter(str => str !== "")[0];
    return document.getElementById(imageElementId).getAttribute("src");
 }

 function focusEditor() {
   document.getElementById('editor').focus();
 }

 function getImageElement(imagePath, imageIdPrefix, alt, width, height) {
    if (imagePath == null || imagePath === '') {
        return '';
    }
    return '<br>'
             + '<div>'
             + '<img'
             + ' id="' + imageIdPrefix + (Editor.numberOfImages) + '"'
             + ' src="' + imagePath + '" '
             + ' width="' + width + '" '
             + ' style="max-width: 99%;"'
             + ' height="' + height + '"/>'
             + '</div>'
             + '<br><br>';
 }

 function getHtml() {
    return document.getElementById('editor').innerHTML;
 }

 /**
  * Returns the currently focused div element node.
  */
 function getFocusedDiv() {
   var currentNode = document.getSelection().anchorNode;
   var node = document.getSelection().anchorNode;
   if (node == null) {
     return null;
   }
   while (node.nodeName !== "DIV") {
       node = node.parentNode;
   }
   return node;
 }

 function createNewDivElement(direction, content) {
    var editor = document.getElementById('editor');
    var newDiv = document.createElement('div');
    newDiv.dir = direction;
    newDiv.contenteditable = true;
    newDiv.innerHTML = content == null || content === '' ? '<br>' : content;
    editor.appendChild(newDiv);
    newDiv.focus();
 }

 function reportEnabledStyles() {
   var activeStyles = [];

   if (document.queryCommandState('bold')) {
       activeStyles.push('BOLD');
   }
   if (document.queryCommandState('italic')) {
       activeStyles.push('ITALIC');
   }
   if (document.queryCommandState('underline')) {
       activeStyles.push('UNDER_LINE');
   }
   if (document.queryCommandState('InsertUnorderedList')) {
       activeStyles.push('BULLET_LIST');
   }
   if (document.queryCommandState('InsertOrderedList')) {
       activeStyles.push('NUMBER_LIST');
   }
   if (document.queryCommandState('strikeThrough')) {
       activeStyles.push('STRIKE_THROUGH');
   }

   JSInterface.onSelectionChanged(getHtml(), activeStyles);
 }

 document.addEventListener("keydown", function() {
    reportEnabledStyles();
 });

 document.addEventListener("selectionchange", function() {
    reportEnabledStyles();
 });
