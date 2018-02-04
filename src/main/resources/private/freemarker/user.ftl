<#include "header.ftl">
<div id="container">
    <h1 style="display: inline">${item2.getFirstname()} ${item2.getLastname()}</h1>
    <h3 style="display: inline">(${item2.getEmail()})</h3>
    <h2>Certifications:</h2>
    <ul>
        <#list item1 as cert>
            <li>${cert.getType()} (Level ${cert.getLevel()})</li>
        </#list>
    </ul>
    <h3>Member Since ${item2.getShortCreatedDate()}</h3>
    <#if item4>
        <h1>THIS IS YOUR PAGE--DO WHAT YOU WISH</h1>
        <!--<a href="/req-cert"><h2>Request Certification</h2></a>-->
    </#if>
</div>
<#include "footer.ftl">
<script type="text/javascript" src="/main.js"></script>