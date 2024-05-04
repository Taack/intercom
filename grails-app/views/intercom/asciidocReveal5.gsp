<%--
  Created by IntelliJ IDEA.
  User: auo
  Date: 27/08/2021
  Time: 05:55
--%>

<%@ page import="intercom.IntercomTheme" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, minimal-ui">
    <style type="text/css">
    .reveal div.right {
        float: right
    }

    /* source blocks */
    .reveal .listingblock.stretch > .content {
        height: 100%
    }

    .reveal .listingblock.stretch > .content > pre {
        height: 100%
    }

    .reveal .listingblock.stretch > .content > pre > code {
        height: 100%;
        max-height: 100%
    }

    /* auto-animate feature */
    /* hide the scrollbar when auto-animating source blocks */
    .reveal pre[data-auto-animate-target] {
        overflow: hidden;
    }

    .reveal pre[data-auto-animate-target] code {
        overflow: hidden;
    }

    /* add a min width to avoid horizontal shift on line numbers */
    code.hljs .hljs-ln-line.hljs-ln-n {
        min-width: 1.25em;
    }

    /* tables */
    table {
        border-collapse: collapse;
        border-spacing: 0
    }

    table {
        margin-bottom: 1.25em;
        border: solid 1px #dedede
    }

    table thead tr th, table thead tr td, table tfoot tr th, table tfoot tr td {
        padding: .5em .625em .625em;
        font-size: inherit;
        text-align: left
    }

    table tr th, table tr td {
        padding: .5625em .625em;
        font-size: inherit
    }

    table thead tr th, table tfoot tr th, table tbody tr td, table tr td, table tfoot tr td {
        display: table-cell;
        line-height: 1.6
    }

    td.tableblock > .content {
        margin-bottom: 1.25em
    }

    td.tableblock > .content > :last-child {
        margin-bottom: -1.25em
    }

    table.tableblock, th.tableblock, td.tableblock {
        border: 0 solid #dedede
    }

    table.grid-all > thead > tr > .tableblock, table.grid-all > tbody > tr > .tableblock {
        border-width: 0 1px 1px 0
    }

    table.grid-all > tfoot > tr > .tableblock {
        border-width: 1px 1px 0 0
    }

    table.grid-cols > * > tr > .tableblock {
        border-width: 0 1px 0 0
    }

    table.grid-rows > thead > tr > .tableblock, table.grid-rows > tbody > tr > .tableblock {
        border-width: 0 0 1px
    }

    table.grid-rows > tfoot > tr > .tableblock {
        border-width: 1px 0 0
    }

    table.grid-all > * > tr > .tableblock:last-child, table.grid-cols > * > tr > .tableblock:last-child {
        border-right-width: 0
    }

    table.grid-all > tbody > tr:last-child > .tableblock, table.grid-all > thead:last-child > tr > .tableblock, table.grid-rows > tbody > tr:last-child > .tableblock, table.grid-rows > thead:last-child > tr > .tableblock {
        border-bottom-width: 0
    }

    table.frame-all {
        border-width: 1px
    }

    table.frame-sides {
        border-width: 0 1px
    }

    table.frame-topbot, table.frame-ends {
        border-width: 1px 0
    }

    .reveal table th.halign-left, .reveal table td.halign-left {
        text-align: left
    }

    .reveal table th.halign-right, .reveal table td.halign-right {
        text-align: right
    }

    .reveal table th.halign-center, .reveal table td.halign-center {
        text-align: center
    }

    .reveal table th.valign-top, .reveal table td.valign-top {
        vertical-align: top
    }

    .reveal table th.valign-bottom, .reveal table td.valign-bottom {
        vertical-align: bottom
    }

    .reveal table th.valign-middle, .reveal table td.valign-middle {
        vertical-align: middle
    }

    table thead th, table tfoot th {
        font-weight: bold
    }

    tbody tr th {
        display: table-cell;
        line-height: 1.6
    }

    tbody tr th, tbody tr th p, tfoot tr th, tfoot tr th p {
        font-weight: bold
    }

    thead {
        display: table-header-group
    }

    .reveal table.grid-none th, .reveal table.grid-none td {
        border-bottom: 0 !important
    }

    /* kbd macro */
    kbd {
        font-family: "Droid Sans Mono", "DejaVu Sans Mono", monospace;
        display: inline-block;
        color: rgba(0, 0, 0, .8);
        font-size: .65em;
        line-height: 1.45;
        background: #f7f7f7;
        border: 1px solid #ccc;
        -webkit-border-radius: 3px;
        border-radius: 3px;
        -webkit-box-shadow: 0 1px 0 rgba(0, 0, 0, .2), 0 0 0 .1em white inset;
        box-shadow: 0 1px 0 rgba(0, 0, 0, .2), 0 0 0 .1em #fff inset;
        margin: 0 .15em;
        padding: .2em .5em;
        vertical-align: middle;
        position: relative;
        top: -.1em;
        white-space: nowrap
    }

    .keyseq kbd:first-child {
        margin-left: 0
    }

    .keyseq kbd:last-child {
        margin-right: 0
    }

    /* callouts */
    .conum[data-value] {
        display: inline-block;
        color: #fff !important;
        background: rgba(0, 0, 0, .8);
        -webkit-border-radius: 50%;
        border-radius: 50%;
        text-align: center;
        font-size: .75em;
        width: 1.67em;
        height: 1.67em;
        line-height: 1.67em;
        font-family: "Open Sans", "DejaVu Sans", sans-serif;
        font-style: normal;
        font-weight: bold
    }

    .conum[data-value] * {
        color: #fff !important
    }

    .conum[data-value] + b {
        display: none
    }

    .conum[data-value]:after {
        content: attr(data-value)
    }

    pre .conum[data-value] {
        position: relative;
        top: -.125em
    }

    b.conum * {
        color: inherit !important
    }

    .conum:not([data-value]):empty {
        display: none
    }

    /* Callout list */
    .hdlist > table, .colist > table {
        border: 0;
        background: none
    }

    .hdlist > table > tbody > tr, .colist > table > tbody > tr {
        background: none
    }

    td.hdlist1, td.hdlist2 {
        vertical-align: top;
        padding: 0 .625em
    }

    td.hdlist1 {
        font-weight: bold;
        padding-bottom: 1.25em
    }

    i.fa, td.content {
        color: #fff;
        text-shadow: 1px 0 0 #000, 0 -1px 0 #000, 0 1px 0 #000, -1px 0 0 #000;
    }

    /* Disabled from Asciidoctor CSS because it caused callout list to go under the
     * source listing when .stretch is applied (see #335)
     * .literalblock+.colist,.listingblock+.colist{margin-top:-.5em} */
    .colist td:not([class]):first-child {
        padding: .4em .75em 0;
        line-height: 1;
        vertical-align: top
    }

    .colist td:not([class]):first-child img {
        max-width: none
    }

    .colist td:not([class]):last-child {
        padding: .25em 0
    }

    /* Override Asciidoctor CSS that causes issues with reveal.js features */
    .reveal .hljs table {
        border: 0
    }

    /* Callout list rows would have a bottom border with some reveal.js themes (see #335) */
    .reveal .colist > table th, .reveal .colist > table td {
        border-bottom: 0
    }

    /* Fixes line height with Highlight.js source listing when linenums enabled (see #331) */
    .reveal .hljs table thead tr th, .reveal .hljs table tfoot tr th, .reveal .hljs table tbody tr td, .reveal .hljs table tr td, .reveal .hljs table tfoot tr td {
        line-height: inherit
    }

    /* Columns layout */
    .columns .slide-content {
        display: flex;
    }

    .columns.wrap .slide-content {
        flex-wrap: wrap;
    }

    .columns.is-vcentered .slide-content {
        align-items: center;
    }

    .columns .slide-content > .column {
        display: block;
        flex-basis: 0;
        flex-grow: 1;
        flex-shrink: 1;
    }

    .columns .slide-content > .column > * {
        padding: .75rem;
    }

    /* See #353 */
    .columns.wrap .slide-content > .column {
        flex-basis: auto;
    }

    .columns .slide-content > .column.is-full {
        flex: none;
        width: 100%;
    }

    .columns .slide-content > .column.is-four-fifths {
        flex: none;
        width: 80%;
    }

    .columns .slide-content > .column.is-three-quarters {
        flex: none;
        width: 75%;
    }

    .columns .slide-content > .column.is-two-thirds {
        flex: none;
        width: 66.6666%;
    }

    .columns .slide-content > .column.is-three-fifths {
        flex: none;
        width: 60%;
    }

    .columns .slide-content > .column.is-half {
        flex: none;
        width: 50%;
    }

    .columns .slide-content > .column.is-two-fifths {
        flex: none;
        width: 40%;
    }

    .columns .slide-content > .column.is-one-third {
        flex: none;
        width: 33.3333%;
    }

    .columns .slide-content > .column.is-one-quarter {
        flex: none;
        width: 25%;
    }

    .columns .slide-content > .column.is-one-fifth {
        flex: none;
        width: 20%;
    }

    .columns .slide-content > .column.has-text-left {
        text-align: left;
    }

    .columns .slide-content > .column.has-text-justified {
        text-align: justify;
    }

    .columns .slide-content > .column.has-text-right {
        text-align: right;
    }

    .columns .slide-content > .column.has-text-left {
        text-align: left;
    }

    .columns .slide-content > .column.has-text-justified {
        text-align: justify;
    }

    .columns .slide-content > .column.has-text-right {
        text-align: right;
    }

    .text-left {
        text-align: left !important
    }

    .text-right {
        text-align: right !important
    }

    .text-center {
        text-align: center !important
    }

    .text-justify {
        text-align: justify !important
    }

    .footnotes {
        border-top: 1px solid rgba(0, 0, 0, 0.2);
        padding: 0.5em 0 0 0;
        font-size: 0.65em;
        margin-top: 4em;
    }

    .byline {
        font-size: .8em
    }

    ul.byline {
        list-style-type: none;
    }

    ul.byline li + li {
        margin-top: 0.25em;
    }
    </style>

    <%
        IntercomTheme theme = theme ?: IntercomTheme.SOLARIZED
        String revealVersion = "5.0.5"
    %>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">

    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/plugin/highlight/monokai.min.css"
          integrity="sha512-z8wQkuDRFwCBfoj7KOiu1MECaRVoXx6rZQWL21x0BsVVH7JkqCp1Otf39qve6CrCycOOL5o9vgfII5Smds23rg=="
          crossorigin="anonymous" referrerpolicy="no-referrer"/>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/plugin/highlight/zenburn.min.css"
          integrity="sha512-JPxjD2t82edI35nXydY/erE9jVPpqxEJ++6nYEoZEpX2TRsmp2FpZuQqZa+wBCen5U16QZOkMadGXHCfp+tUdg=="
          crossorigin="anonymous" referrerpolicy="no-referrer"/>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/theme/fonts/league-gothic/league-gothic.min.css"
          integrity="sha512-TtFDuyklCBQEnImHtcCEcFvpXEK7C2HiHwZY5V7TewP3r2PIeMRwf6SQltD47ixdcRzSZiEvFfV5au9Qx+/d+Q=="
          crossorigin="anonymous" referrerpolicy="no-referrer"/>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/theme/fonts/source-sans-pro/source-sans-pro.min.css"
          integrity="sha512-3Xywo2OI5FqQh0A8U4NwmEYP15dM8LQ33MLqNqTwxYfurqQ5Mx+eYfjKO6QAkS0dPUSp6Q/S7e7c+8qZF6s9Lw=="
          crossorigin="anonymous" referrerpolicy="no-referrer"/>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/reset.min.css"
          integrity="sha512-Mjxkx+r7O/OLQeKeIBCQ2yspG1P5muhAtv/J+p2/aPnSenciZWm5Wlnt+NOUNA4SHbnBIE/R2ic0ZBiCXdQNUg=="
          crossorigin="anonymous" referrerpolicy="no-referrer"/>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/reveal.min.css"
          integrity="sha512-RKxUI4ygYACvysn5B5Oo+NBc6W3MXdMreutDYeoCLhJSfAkUqp3TWvIInio/eR2YVhjIkLrDNL1WaL8u/Z7XUw=="
          crossorigin="anonymous"
          referrerpolicy="no-referrer"/>
    <g:if test="${theme == IntercomTheme.BEIGE}">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/theme/beige.min.css"
              integrity="sha512-+ghJksoFFsor/FKiXupUTqG1SzBhdXTFsVtIO6nmfXo2S/YwfNfL6Qd6QnEcjQwfLAO1/ED6c99t6W1X/4+YAQ=="
              crossorigin="anonymous" referrerpolicy="no-referrer"/>
    </g:if>
    <g:if test="${theme == IntercomTheme.BLACK}">
        <link rel="stylesheet"
              href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/theme/black-contrast.min.css"
              integrity="sha512-QWX1pIBsBG4YN+6+P2WXEtQCQAdPAL6O0mFTbolQy7Uk72D1cZxKa+bCNzPsCOGGMHrOJCuv/UXMmez7WH0qpA=="
              crossorigin="anonymous" referrerpolicy="no-referrer"/>
    </g:if>
    <g:if test="${theme == IntercomTheme.BLOOD}">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/theme/blood.min.css"
              integrity="sha512-dbf3UHwIYeKButfMjZiDxTTBsma6tg91R0EbdqMzHOm7nC9r7xTiZabuf0o7UlHwBuaoImwvt2n/Yfz+I3ql/w=="
              crossorigin="anonymous" referrerpolicy="no-referrer"/>
    </g:if>
    <g:if test="${theme == IntercomTheme.MONOKAI}">
        <link rel="stylesheet"
              href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/plugin/highlight/monokai.min.css"
              integrity="sha512-z8wQkuDRFwCBfoj7KOiu1MECaRVoXx6rZQWL21x0BsVVH7JkqCp1Otf39qve6CrCycOOL5o9vgfII5Smds23rg=="
              crossorigin="anonymous" referrerpolicy="no-referrer"/>
    </g:if>
    <g:if test="${theme == IntercomTheme.ZENBURN}">
        <link rel="stylesheet"
              href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/plugin/highlight/zenburn.min.css"
              integrity="sha512-JPxjD2t82edI35nXydY/erE9jVPpqxEJ++6nYEoZEpX2TRsmp2FpZuQqZa+wBCen5U16QZOkMadGXHCfp+tUdg=="
              crossorigin="anonymous" referrerpolicy="no-referrer"/>
    </g:if>
    <g:if test="${theme == IntercomTheme.SOLARIZED}">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/theme/solarized.min.css"
              integrity="sha512-V8VzH1oZOKksN3BHgMaqU9mZBqST6lh6CKSGOz4/pXjOHXHvm4NdX753i8ImR18/IpQqXKG2mUUrnB/zc3nFHQ=="
              crossorigin="anonymous" referrerpolicy="no-referrer"/>
    </g:if>
    <g:if test="${theme == IntercomTheme.SKY}">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/theme/sky.min.css"
              integrity="sha512-c34A00Y4Cg1wtImu9Qf2xUX0nFSmdI8xDTQYpSauf1ZCYJeRW4p1J+obK/i/bbzKrhxrv4Dk/x2EuLSD3YnEZg=="
              crossorigin="anonymous" referrerpolicy="no-referrer"/>
    </g:if>
    <g:if test="${theme == IntercomTheme.SIMPLE}">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/theme/simple.min.css"
              integrity="sha512-RRWSpiIK5YG93Pg5fbZs/Rrs3iD4l4RHkQURh9mMmGEOhxA1IhVMjy63ehWXuUYtjzwXs/CPGUzjIaSmNumedw=="
              crossorigin="anonymous" referrerpolicy="no-referrer"/>
    </g:if>

    <style>
    #asciidoctor {
        letter-spacing: normal;
    }
    </style>
</head>

<body>

<div class="reveal">
    <div class="slides">
        ${raw(pageAsciidocContent)}
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/reveal.js"
        integrity="sha512-miEtcml/7wkksTvGaVFxhtGkok9wRC+VT/0d9xHAiLP3s9Z788nsSB1BvOWCn6Thj5kTjigPB6W1pSP5LCkMIw=="
        crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/plugin/math/math.min.js"
        integrity="sha512-skPZpuRwuUAnF9iEEFBXc4zJaucKcHUDgY1wDBTv0ILy82C2gn8MJsbcinzj2u8r/iZjD/78HRgw2/n//poOhQ=="
        crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/plugin/zoom/zoom.min.js"
        integrity="sha512-Gras1ky8LoFJWwMTxBWyN2wfHfnJXQlyhHFH3M+m/jHe297DZsrQxg9P6Kxka6waxl4NeeQzietoFlCxL7x10g=="
        crossorigin="anonymous" referrerpolicy="no-referrer"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/5.0.5/plugin/highlight/highlight.min.js"
        integrity="sha512-xkVKkN0o7xECTHSUZ9zdsBYRXiAKH7CZ3aICpW6aQJZsufVVRLhEBTDjTpC1tPzm+gNZiOeW174zXAB2fOLsTg=="
        crossorigin="anonymous" referrerpolicy="no-referrer"></script>


<script>Array.prototype.slice.call(document.querySelectorAll('.slides section')).forEach(function (slide) {
    if (slide.getAttribute('data-background-color')) return;
    // user needs to explicitly say he wants CSS color to override otherwise we might break custom css or theme (#226)
    if (!(slide.classList.contains('canvas') || slide.classList.contains('background'))) return;
    var bgColor = getComputedStyle(slide).backgroundColor;
    if (bgColor !== 'rgba(0, 0, 0, 0)' && bgColor !== 'transparent') {
        slide.setAttribute('data-background-color', bgColor);
        slide.style.backgroundColor = 'transparent';
    }
});

// More info about config & dependencies:
// - https://github.com/hakimel/reveal.js#configuration
// - https://github.com/hakimel/reveal.js#dependencies
Reveal.initialize({
    // Display presentation control arrows
    controls: true,
    // Help the user learn the controls by providing hints, for example by
    // bouncing the down arrow when they first encounter a vertical slide
    controlsTutorial: true,
    // Determines where controls appear, "edges" or "bottom-right"
    controlsLayout: 'bottom-right',
    // Visibility rule for backwards navigation arrows; "faded", "hidden"
    // or "visible"
    controlsBackArrows: 'faded',
    // Display a presentation progress bar
    progress: true,
    // Display the page number of the current slide
    slideNumber: 'true',
    // Control which views the slide number displays on
    showSlideNumber: 'all',
    // Add the current slide number to the URL hash so that reloading the
    // page/copying the URL will return you to the same slide
    hash: false,
    // Push each slide change to the browser history. Implies `hash: true`
    history: true,
    // Enable keyboard shortcuts for navigation
    keyboard: true,
    // Enable the slide overview mode
    overview: true,
    // Disables the default reveal.js slide layout so that you can use custom CSS layout
    disableLayout: false,
    // Vertical centering of slides
    center: false,
    // Enables touch navigation on devices with touch input
    touch: true,
    // Loop the presentation
    loop: false,
    // Change the presentation direction to be RTL
    rtl: false,
    // See https://github.com/hakimel/reveal.js/#navigation-mode
    navigationMode: 'default',
    // Randomizes the order of slides each time the presentation loads
    shuffle: false,
    // Turns fragments on and off globally
    fragments: true,
    // Flags whether to include the current fragment in the URL,
    // so that reloading brings you to the same fragment position
    fragmentInURL: false,
    // Flags if the presentation is running in an embedded mode,
    // i.e. contained within a limited portion of the screen
    embedded: false,
    // Flags if we should show a help overlay when the questionmark
    // key is pressed
    help: true,
    // Flags if speaker notes should be visible to all viewers
    showNotes: false,
    // Global override for autolaying embedded media (video/audio/iframe)
    // - null: Media will only autoplay if data-autoplay is present
    // - true: All media will autoplay, regardless of individual setting
    // - false: No media will autoplay, regardless of individual setting
    autoPlayMedia: null,
    // Global override for preloading lazy-loaded iframes
    // - null: Iframes with data-src AND data-preload will be loaded when within
    //   the viewDistance, iframes with only data-src will be loaded when visible
    // - true: All iframes with data-src will be loaded when within the viewDistance
    // - false: All iframes with data-src will be loaded only when visible
    preloadIframes: null,
    // Number of milliseconds between automatically proceeding to the
    // next slide, disabled when set to 0, this value can be overwritten
    // by using a data-autoslide attribute on your slides
    autoSlide: 0,
    // Stop auto-sliding after user input
    autoSlideStoppable: true,
    // Use this method for navigation when auto-sliding
    autoSlideMethod: Reveal.navigateNext,
    // Specify the average time in seconds that you think you will spend
    // presenting each slide. This is used to show a pacing timer in the
    // speaker view
    defaultTiming: 120,
    // Specify the total time in seconds that is available to
    // present.  If this is set to a nonzero value, the pacing
    // timer will work out the time available for each slide,
    // instead of using the defaultTiming value
    totalTime: 0,
    // Specify the minimum amount of time you want to allot to
    // each slide, if using the totalTime calculation method.  If
    // the automated time allocation causes slide pacing to fall
    // below this threshold, then you will see an alert in the
    // speaker notes window
    minimumTimePerSlide: 0,
    // Enable slide navigation via mouse wheel
    mouseWheel: true,
    // Hide cursor if inactive
    hideInactiveCursor: true,
    // Time before the cursor is hidden (in ms)
    hideCursorTime: 5000,
    // Hides the address bar on mobile devices
    hideAddressBar: true,
    // Opens links in an iframe preview overlay
    // Add `data-preview-link` and `data-preview-link="false"` to customise each link
    // individually
    previewLinks: false,
    // Transition style (e.g., none, fade, slide, convex, concave, zoom)
    transition: 'default',
    // Transition speed (e.g., default, fast, slow)
    transitionSpeed: 'default',
    // Transition style for full page slide backgrounds (e.g., none, fade, slide, convex, concave, zoom)
    backgroundTransition: 'default',
    // Number of slides away from the current that are visible
    viewDistance: 3,
    // Number of slides away from the current that are visible on mobile
    // devices. It is advisable to set this to a lower number than
    // viewDistance in order to save resources.
    mobileViewDistance: 3,
    // Parallax background image (e.g., "'https://s3.amazonaws.com/hakim-static/reveal-js/reveal-parallax-1.jpg'")
    parallaxBackgroundImage: '',
    // Parallax background size in CSS syntax (e.g., "2100px 900px")
    parallaxBackgroundSize: '',
    // Number of pixels to move the parallax background per slide
    // - Calculated automatically unless specified
    // - Set to 0 to disable movement along an axis
    parallaxBackgroundHorizontal: null,
    parallaxBackgroundVertical: null,
    // The display mode that will be used to show slides
    display: 'block',

    // The "normal" size of the presentation, aspect ratio will be preserved
    // when the presentation is scaled to fit different resolutions. Can be
    // specified using percentage units.
    width: 960,
    height: 700,

    // Factor of the display size that should remain empty around the content
    margin: 0.1,

    // Bounds for smallest/largest possible scale to apply to content
    minScale: 0.2,
    maxScale: 1.5,

    // PDF Export Options
    // Put each fragment on a separate page
    pdfSeparateFragments: true,
    // For slides that do not fit on a page, max number of pages
    pdfMaxPagesPerSlide: 1,

    // Optional libraries used to extend on reveal.js
    plugins: [RevealMath, RevealZoom, RevealHighlight],
});</script>




<!--<script>
    Reveal.initialize({
        plugins: [RevealMath, RevealZoom, RevealHighlight]
    });
</script>-->
</body>
</html>