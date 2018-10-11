package com.airbus.boa.view.item;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.airbus.boa.entity.article.Board;
import com.airbus.boa.service.NavigationConstants;
import com.airbus.boa.util.MessageBundle;

@ManagedBean(name = BoardController.BEAN_NAME)
@ViewScoped
public class BoardController extends AbstractArticleController {
    
    private static final long serialVersionUID = 1L;
    
    /** The managed bean name */
    public static final String BEAN_NAME = "boardController";

    public BoardController() {
        itemPage = NavigationConstants.BOARD_MANAGEMENT_PAGE;
        listPage = NavigationConstants.BOARD_LIST_PAGE;
    }
    
    @Override
    protected void initItemWithNew() {
        article = new Board();
    }
    
    @Override
    protected void initItemFromDatabase() {
        article = articleBean.findArticleById(articleId);
        if (!(article instanceof Board)) {
            article = null;
        }
    }
    
    @Override
    protected String getCreateTitle() {
        return MessageBundle.getMessage("createBoardTitle");
    }
    
    @Override
    protected String getReadTitle() {
        return MessageBundle.getMessage("infoBoardTitle");
    }
    
    @Override
    protected String getUpdateTitle() {
        return MessageBundle.getMessage("updateBoardTitle");
    }
    
}
