package com.pine.player.applet.barrage.bean;

/**
 * Created by tanghongfeng on 2018/2/1.
 */

public class PartialDisplayBarrageNode {
    private PartialDisplayBarrageNode preNode;
    private PartialDisplayBarrageNode nextNode;
    private int startPixIndex;
    private int nodeUsedPix;
    private int untilNextRemainderPix;

    public PartialDisplayBarrageNode() {

    }

    public PartialDisplayBarrageNode(PartialDisplayBarrageNode preNode,
                                     PartialDisplayBarrageNode nextNode, int startPixIndex,
                                     int nodeUsedPix, int untilNextRemainderPix) {
        this.preNode = preNode;
        this.nextNode = nextNode;
        this.startPixIndex = startPixIndex;
        this.nodeUsedPix = nodeUsedPix;
        this.untilNextRemainderPix = untilNextRemainderPix;
    }

    public PartialDisplayBarrageNode getPreNode() {
        return preNode;
    }

    public void setPreNode(PartialDisplayBarrageNode preNode) {
        this.preNode = preNode;
    }

    public PartialDisplayBarrageNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(PartialDisplayBarrageNode nextNode) {
        this.nextNode = nextNode;
    }

    public int getStartPixIndex() {
        return startPixIndex;
    }

    public void setStartPixIndex(int startPixIndex) {
        this.startPixIndex = startPixIndex;
    }

    public int getNodeUsedPix() {
        return nodeUsedPix;
    }

    public void setNodeUsedPix(int nodeUsedPix) {
        this.nodeUsedPix = nodeUsedPix;
    }

    public int getUntilNextRemainderPix() {
        return untilNextRemainderPix;
    }

    public void setUntilNextRemainderPix(int untilNextRemainderPix) {
        this.untilNextRemainderPix = untilNextRemainderPix;
    }

    @Override
    public String toString() {
        return "{startPixIndex: " + startPixIndex
                + ", nodeUsedPix:" + nodeUsedPix
                + ", untilNextRemainderPix:" + untilNextRemainderPix
                + "}";
    }
}
