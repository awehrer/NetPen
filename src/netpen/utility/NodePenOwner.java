/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netpen.utility;

import java.util.Arrays;
import java.util.Collection;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import jpen.PenProvider;
import jpen.owner.AbstractPenOwner;
import jpen.owner.PenClip;
import jpen.provider.osx.CocoaProvider;
import jpen.provider.wintab.WintabProvider;
import jpen.provider.xinput.XinputProvider;

/**
 *
 * @author Arbor
 */
public class NodePenOwner extends AbstractPenOwner
{
    private final Node node;
    private final NodePenClip penClip;
    
    public NodePenOwner(Node node)
    {
        this.node = node;
        this.penClip = new NodePenClip(this);
    }
    
    public Node getNode()
    {
        return node;
    }

    @Override
    protected void draggingOutDisengaged()
    {
        // nothing
    }

    @Override
    protected void init()
    {
        getNode().setOnMouseEntered((MouseEvent event) -> {
            synchronized (penManagerHandle.getPenSchedulerLock())
            {
                if (!stopDraggingOut())
                {
                    penManagerHandle.setPenManagerPaused(false);
                }
            }
        });
        
        getNode().setOnMouseExited((MouseEvent event) -> {
            synchronized (penManagerHandle.getPenSchedulerLock())
            {
                if (!startDraggingOut())
                {
                    penManagerHandle.setPenManagerPaused(true);
                }
            }
        });
    }

    @Override
    public final Collection<PenProvider.Constructor> getPenProviderConstructors()
    {
        return Arrays.asList(
                new PenProvider.Constructor[]
                {
                        new XinputProvider.Constructor(),
                        new WintabProvider.Constructor(),
                        new CocoaProvider.Constructor()
                }
        );
    }

    @Override
    public PenClip getPenClip()
    {
        return penClip;
    }
}
