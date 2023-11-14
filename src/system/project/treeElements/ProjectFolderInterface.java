package system.project.treeElements;

//====================================================================================================
// Authors: Hikaito
// Project: Fox Engine
//====================================================================================================

import system.project.treeElements.ProjectUnitCore;

import java.util.LinkedList;

public interface ProjectFolderInterface {
    public LinkedList<ProjectUnitCore> getChildren();
}
