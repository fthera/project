var currentViewState;
jsf.ajax.addOnEvent(function(e){
    var xml = e.responseXML;
    var source = e.source;
    var status = e.status;
    if(status === 'success'){
        var response = xml.getElementsByTagName('partial-response')[0];
        if(response !== null){
            var changes = response.getElementsByTagName('changes')[0];
            if(changes !== null){
                var updates = changes.getElementsByTagName('update');
                if(updates !== null){
                    for(var i = 0; i< updates.length; i++){
                        var update = updates[i];
                        var id = update.getAttribute('id');
                        if(id.indexOf('javax.faces.ViewState') > -1){
                            currentViewState = update.firstChild.data;
                        }
                    }
                    for(var i = 0; i< updates.length; i++){
                        var update = updates[i];
                        var id = update.getAttribute('id');
                        if(id.indexOf('javax.faces.ViewState') == -1){
                            var target = document.getElementById(update.getAttribute('id'));
                            var forms = target.getElementsByTagName("form");
                            for(var j = 0; j < forms.length; j++){
                                var form = forms[j];
                                var field = form.elements["javax.faces.ViewState"];
                                if (typeof field == 'undefined') {
                                    field = document.createElement("input");
                                    field.type = "hidden";
                                    field.name = "javax.faces.ViewState";
                                    form.appendChild(field);
                                }
                                field.value = currentViewState;
                            }
                        }
                    }
                }
	        }
	    }
	}
});

function patchModal() {
	$('.rf-pp-cntr').parent('div').each(function(){
		if($(this).css('visibility') === 'hidden'){
			$(this).css('position', 'absolute');
			$(this).css('top', '0px');
		}
	});
};


$(document).ready(function (){
	patchModal();
});
