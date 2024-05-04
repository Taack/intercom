<%--
  Created by IntelliJ IDEA.
  User: auo
  Date: 27/08/2021
  Time: 05:55
--%>

<%@ page import="intercom.IntercomTheme" contentType="text/html;charset=UTF-8" %>
<html>
<head>

    <%
        IntercomTheme theme = theme ?: IntercomTheme.SOLARIZED
    %>


    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/reveal.min.css" integrity="sha512-WFGU7IgfYR0dq5aORzbD+NApAXdExNZFb7LaoO8olYImBW/iZxAwjKEuT+oYcFR6gOd+DAFssq/icMn8YVbQxQ==" crossorigin="anonymous" referrerpolicy="no-referrer" />
<g:if test="${theme == IntercomTheme.BEIGE}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/theme/beige.min.css" integrity="sha512-TzNEhkjNeiWrW7MYy7MlzBqBxqD7Ho7HtFpcW0rAQW4OK8pwZ37fvHKKpJPAmUdHbUnmmKdGwhB5ulLWgQ/l0g==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</g:if>
<g:if test="${theme == IntercomTheme.BLACK}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/theme/black.min.css" integrity="sha512-DKeDMgkMDBNgY3g8T6H6Ft5cB7St0LOh5d69BvETIcTrP0E3d3KhANTMs5QOTMnenXy6JVKz/tENmffCLeXPiQ==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</g:if>
<g:if test="${theme == IntercomTheme.BLOOD}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/theme/blood.min.css" integrity="sha512-rW57Zu9aMzwqAo5AyPqx9AcN+r1JawUBBFxt5RcJS23sh1kX3KeXJYsDBTZbeV2f11jHCoyW5zmnBChL4LFxxQ==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</g:if>
<g:if test="${theme == IntercomTheme.MONOKAI}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/plugin/highlight/monokai.min.css" integrity="sha512-z8wQkuDRFwCBfoj7KOiu1MECaRVoXx6rZQWL21x0BsVVH7JkqCp1Otf39qve6CrCycOOL5o9vgfII5Smds23rg==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</g:if>
<g:if test="${theme == IntercomTheme.ZENBURN}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/plugin/highlight/zenburn.min.css" integrity="sha512-JPxjD2t82edI35nXydY/erE9jVPpqxEJ++6nYEoZEpX2TRsmp2FpZuQqZa+wBCen5U16QZOkMadGXHCfp+tUdg==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</g:if>
<g:if test="${theme == IntercomTheme.SOLARIZED}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/theme/solarized.min.css" integrity="sha512-sUF1FAUpi9yPXCDOPsRwzh71zrCVkcT4SfwxBlQeHwMbH1aTGcSdI00GRLaH6iXRSBTazGH/u6sGQTc1tGqofg==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</g:if>
<g:if test="${theme == IntercomTheme.SKY}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/theme/sky.min.css" integrity="sha512-Y5vz+5wI9ALJMccqguHMlI25sV/YjGIBUYr7yOkYm9/TNQoOlWXWtmzfmQoNVVqYLNkt9NHKl+0ODDQ5+VxVIA==" crossorigin="anonymous" referrerpolicy="no-referrer" />
</g:if>
<g:if test="${theme == IntercomTheme.SIMPLE}">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/reveal.js/4.1.2/theme/simple.min.css" integrity="sha512-Lne04Eg90RC14WjiJWVF4NWfJZdhuiKOKYcn0ljotlSbe6Cksr32AxRZ4vWOPUnMGlOqLLO2MN4/MK3oQlbokA==" crossorigin="anonymous" referrerpolicy="no-referrer" />
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

<script src="https://cdn.jsdelivr.net/npm/reveal.js@4.1.2"></script>
<script>
    Reveal.initialize();
</script>
</body>
</html>