<!doctype html>
<html>
<head>
    <meta name="layout" content="taack" />
    <asset:stylesheet src="intercom.css"/>
    <asset:javascript src="custom-reveal.js"/>
    <script type="text/x-mathjax-config">
    MathJax.Hub.Config({
       asciimath2jax: {
           delimiters: [['\\$','\\$'], ['`','`']]
       }
    });
</script>
    <script type="text/javascript" async
            src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.5/MathJax.js?config=TeX-MML-AM_CHTML">
    </script>

    <style>
    #asciidoctor {
        letter-spacing: normal;
    }
    </style>
</head>
<body>
<main id="asciidocMain" class="article">
%{--    <div id="asciidoctor" class="asciidoctor">--}%
${raw(pageAsciidocContent)}
%{--    </div>--}%
</main>

<script postExecute="true">
    function manageAsciidoc() {
        const asciidocMain = document.getElementById("asciidocMain")
        let divModal = asciidocMain.parentElement;
        const navbar = document.getElementById("toc");
        if (!navbar) return;

        navbar.classList.add("toc2")
        const asciidoctor = document.getElementById("asciidoctor");
        window.onscroll = function () {
            if (asciidoctor && asciidoctor.querySelector("#content")) scrollWindow();
        };
        window.onresize = function () {
            if (navbar) resizeWindow()
        };

        let stickyLeft = navbar?.offsetLeft;
        let stickyMode = divModal.clientWidth > 768;
        let navbarItems = navbar?.getElementsByTagName('a');
        let scrollItems = new Array(navbarItems?.length);
        let sticky = navbar.offsetTop;
        if (navbarItems) {
            for (let i = 0; i < navbarItems.length; i++) {
                let item = navbarItems[i];
                scrollItems.push(item.attributes['href'].value);
            }
        }
        let lastId = "";

        function resizeWindow() {
            sticky = navbar.offsetTop;

            if (navbar && !navbar.classList.contains("sticky")) stickyLeft = navbar?.offsetLeft;
            stickyMode = divModal.clientWidth > 768;
            if (!stickyMode && navbar) {
                navbar.classList.remove("sticky");
                navbar.style.removeProperty('left');
            }
        }

        function scrollWindow() {
            console.log(window.scrollY, sticky)
            if (stickyMode && navbar && asciidoctor) {
                let asciidoctorLeft = asciidoctor.offsetLeft;
                if (window.scrollY >= sticky) {
                    navbar.classList.add("sticky");
                    navbar.style.left = asciidoctorLeft + "px";
                } else {
                    navbar.classList.remove("sticky");
                    navbar.style.removeProperty('left');
                }
                let topElement = asciidoctor.querySelector("#content");
                let fromTop = window.scrollY - topElement.offsetTop;
                let toBottom = window.scrollY - topElement.offsetTop + asciidocMain.clientHeight;
                // let fromTop = window.scrollY;
                let cur;
                let hasCur = scrollItems.some(item => {
                    const si = document.getElementById(item.substring(1));
                    //console.log(si, si.offsetTop, si.offsetTop)
                    if (si.offsetTop >= fromTop && si.offsetTop < toBottom) {
                        cur = item;
                        return true;
                    }
                });
                if (hasCur) {
                    if (lastId !== cur) {
                        lastId = cur;
                        for (let i = 0; i < navbarItems.length; i++) {
                            let item = navbarItems[i];
                            item.classList.remove("active");
                            if (item.attributes["href"].value === lastId) {
                                item.classList.add("active");
                            }
                        }
                    }
                }
            }
        }
    }

    manageAsciidoc()
</script>
</body>
</html>