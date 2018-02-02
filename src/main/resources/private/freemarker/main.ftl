<#include "header.ftl">

<div id="container">
    <table id="judge-list">
        <thead>
            <td>First</td>
            <td>Last</td>
            <td>Affiliation</td>
            <td>Speed</td>
            <td>Flowing</td>
            <td>Details</td>
        </thead>
        <tbody>
        <#list judges as judge>
            <tr class="link-row" data-href="/profile/${judge.getId()}">
                <td>${judge.getFirstname()}</td>
                <td>${judge.getLastname()}</td>
                <#if judge.getAffiliation()??>
                    <td>${judge.getAffiliation()}</td>
                    <#else>
                    <td>(none)</td>
                </#if>
                <td>${judge.getSpeed()}</td>
                <td>${judge.getFlowing()}</td>
                <td><a href="/profile/${judge.getId()}">More Details...</a></td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>
<p hidden class="sampleClass">
<#include "footer.ftl">
<script type="text/javascript" src="/main.js"></script>