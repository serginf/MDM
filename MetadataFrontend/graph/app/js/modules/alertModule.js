
module.exports =  function (graph) {
    /** variable defs **/
    var alertModule={};
    var superContainer=d3.select("#AlertMessages");
    var _messageContainers=[];
    var _messageContext=[];
    var _visibleStatus=[];

    var _filterHintId;
    var _editorHintId;
    var _messageId=-1;
    superContainer.style("display", "inline-block");
    var cssStyleIndex=0;

    function findCSS_Index(){
        var css=document.styleSheets[1].cssRules;
        for (var i=0;i<css.length;i++){
            var entry=css[i];
            if (entry.name==="msg_CollapseAnimation"){
                cssStyleIndex=i;
            }
        }
    }
    findCSS_Index();

    alertModule.addMessageBox=function(){
        // add a container;
        _messageId++;
        var messageContainer=d3.select("#AlertMessages").append("div");
        messageContainer.node().id="messageAContainerId_"+_messageId;

        var messageContext=messageContainer.append("div");
        messageContext.node().id="messageAContextId_"+_messageId;
        messageContext.style("top","0");
        messageContainer.style("position","relative");
        messageContainer.style("width","100%");
        //save in array
        _messageContainers.push(messageContainer);
        _messageContext.push(messageContext);

        // add animation to the container
        messageContainer.node().addEventListener("animationend", function () {
            var containerId=this.id;
            var tokens=containerId.split("_")[1];
            var mContainer=d3.select("#"+containerId);
            // get number of children
            mContainer.classed("hidden",!_visibleStatus[tokens]);
            // clean up DOM
            if (!_visibleStatus[tokens]) {
                mContainer.remove();
                _messageContext[tokens]=null;
                _messageContainers[tokens]=null;
            }
        });

        // set visible flag that is used in end of animation
        _visibleStatus[_messageId]=true;
        return _messageId;
    };
    alertModule.closeMessage=function(id){
        var nId;
        if (id===undefined ){
            var givenId=this.id;
            nId=givenId.split("_")[1];
        } else {
            nId=id;
        }
        if (id && id.indexOf("_")!==-1){
            nId=id.split("_")[1];
        }
        _visibleStatus[nId]=false;
        // get module;
        var moduleContainer=_messageContainers[nId];
        moduleContainer.style("-webkit-animation-name","warn_CollapseAnimation");
        moduleContainer.style("-webkit-animation-duration","0.5s");

        var m_height=moduleContainer.node().getBoundingClientRect().height;

        // find my id in the children
        var pNode=moduleContainer.node().parentNode;

        var followingChildren=[];
        var pChild=pNode.children;
        var pChild_len=pChild.length;
        var containerId=moduleContainer.node().id;
        var found_me=false;
        for (var i=0;i<pChild_len;i++){
            if (found_me===true){
                followingChildren.push(pChild[i].id);
            }

            if (containerId===pChild[i].id){
                found_me=true;
            }

        }

        for (var fc=0; fc<followingChildren.length;fc++){
            var child=d3.select("#"+followingChildren[fc]);
            // get the document style and overwrite it;
            var superCss=document.styleSheets[1].cssRules[cssStyleIndex];
            // remove the existing 0% and 100% rules
            superCss.deleteRule("0%");
            superCss.deleteRule("100%");

            superCss.appendRule("0%   {top: 0;}");
            superCss.appendRule("100% {top: -"+m_height+"px;");

            child.style("-webkit-animation-name","msg_CollapseAnimation");
            child.style("-webkit-animation-duration","0.5s");
            child.node().addEventListener("animationend", function () {
                var c=d3.select(this);
                c.style("-webkit-animation-name","");
                c.style("-webkit-animation-duration","");
            });
        }
    };



    alertModule.showAlert=function(title,msg){
        var id=alertModule.addMessageBox();
        var AlertContainer=_messageContext[id];
        var moduleContainer=_messageContainers[id];
        _visibleStatus[id]=true;

        // add new one;
        var graphWidth=0.5*graph.options().width();

        if (title.length>0){
            var head= AlertContainer.append("div");
            head.style("padding","5px");
            var titleHeader=head.append("div");
            // some classes
            titleHeader.style("display","inline-flex");
            titleHeader.node().innerHTML="<b>"+title+"</b>";
            titleHeader.style("padding-right","3px");
        }
        if (msg.length>0){
            var reasonContainer= AlertContainer.append("div");
            reasonContainer.style("padding","5px");
            var reasonHeader=reasonContainer.append("div");
            // some classes
            reasonHeader.style("display","inline-flex");
            reasonHeader.style("padding-right","3px");

            reasonHeader.node().innerHTML="<b>Message:</b>";
            var msgReason=reasonContainer.append("div");
            // some classes
            msgReason.style("display","inline-flex");
            msgReason.style("max-width",graphWidth+"px");
            msgReason.node().innerHTML=msg;
        }
        var gotItButton;

            gotItButton= AlertContainer.append("label");
            gotItButton.node().id = "killAlertMessages_"+id;
            gotItButton.node().innerHTML = "Got It";
            gotItButton.on("click",alertModule.closeMessage);


        moduleContainer.classed("hidden",false);
        moduleContainer.style("-webkit-animation-name","warn_ExpandAnimation");
        moduleContainer.style("-webkit-animation-duration","0.5s");
        moduleContainer.classed("hidden",false);
    };

    return alertModule;
};


