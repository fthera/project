package com.airbus.boa.view.item;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.airbus.boa.entity.article.Rack;
import com.airbus.boa.entity.article.Switch;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;

@ManagedBean(name = RackController.BEAN_NAME)
@ViewScoped
public class RackController extends AbstractArticleController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "rackController";

    public RackController() {
        itemPage = NavigationConstants.RACK_MANAGEMENT_PAGE;
        listPage = NavigationConstants.RACK_LIST_PAGE;
    }
    
    @Override
    protected void initItemWithNew() {
        article = new Rack();
    }
    
    @Override
    protected void initItemFromDatabase() {
        article = articleBean.findArticleById(articleId);
        if (!(article instanceof Rack) || (article instanceof Switch)) {
            article = null;
        }
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createRackTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoRackTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateRackTitle");
    }
    
}
