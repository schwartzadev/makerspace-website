<#include "header.ftl">


<div id="container">
    <form id="form-root" method="post" action="/make-profile">
        <h2>Add a new judge...</h2>
        <div>
            <label for="first">Judge's First Name</label>
            <input name="first" id="first" type="text">
        </div>
        <div>
            <label for="last">Judge's Last Name</label>
            <input name="last" id="last" type="text" required>
        </div>
        <div>
            <label for="affiliation">Judge's School Affiliation *</label>
            <input name="affiliation" id="affiliation" type="text">
        </div>
        <div>
            <label for="speed">Comfort with Speed (1-5)</label>
            <input name="speed" id="speed" type="number" min="1" max="5" required>
        </div>
        <div>
            <label for="flow">Judge's Amount of Flowing (1-5)</label>
            <input name="flow" id="flow" type="number" min="1" max="5" required>
        </div>
        <div>
            <label for="paradigm-link">Judge Paradigm on <a href="https://judgephilosophies.wikispaces.com">JudgePhilosophies</a> or <a href="https://www.tabroom.com/index/paradigm.mhtml">Tabroom</a> *</label>
            <input name="paradigm-link" id="paradigm-link" type="text">
        </div>
        <div>
            <label for="paradigm">What was your judge's self-described paradigm?</label>
            <br>
            <textarea rows="8" cols="45" id="paradigm" name="paradigm" required></textarea>
        </div>
        <div>
            <label for="voteson">What did the judge vote on?</label>
            <br>
            <textarea rows="4" cols="25" id="voteson" name="voteson" required></textarea>
        </div>
        <button class="submit" >Submit</button>
    </form>
    <p>* means an optional field</p>
</div>