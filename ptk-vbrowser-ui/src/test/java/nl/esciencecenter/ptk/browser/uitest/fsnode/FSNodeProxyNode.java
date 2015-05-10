/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.browser.uitest.fsnode;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.LongHolder;
import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.io.FSPath;
import nl.esciencecenter.ptk.io.FSUtil;
import nl.esciencecenter.ptk.io.exceptions.FileURISyntaxException;
import nl.esciencecenter.ptk.presentation.Presentation;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyException;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.data.Attribute;
import nl.esciencecenter.vbrowser.vrs.mimetypes.MimeTypes;
import nl.esciencecenter.vbrowser.vrs.registry.ResourceConfigInfo;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Example ProxyNode based on (Generic) FSNode class.
 */
public class FSNodeProxyNode extends ProxyNode {
    FSPath file;

    private FSNodeAttributes metaFile;

    protected FSNodeProxyNode createChild(String childname) throws ProxyException {
        return new FSNodeProxyNode(getProxyFactory(), getVRL().appendPath(childname));
    }

    public FSNodeProxyNode(ProxyFactory anyFileProxyFactory, VRL loc) throws ProxyException {
        super(anyFileProxyFactory, loc);
        try {
            file = FSUtil.getDefault().newFSPath(loc.getPath());
        } catch (IOException e) {
            throw new ProxyException(e.getMessage(), e);
        }
        init();
    }

    public FSNodeProxyNode(ProxyFactory anyFileProxyFactory, VRL loc, FSPath file)
            throws ProxyException {
        super(anyFileProxyFactory, loc);
        this.file = file;
        init();
    }

    protected FSNodeProxyNode(ProxyFactory anyFileProxyFactory, FSNodeProxyNode parent, VRL locator)
            throws ProxyException {
        super(anyFileProxyFactory, locator);
        init();
    }

    private void init() throws ProxyException {
        this.metaFile = new FSNodeAttributes(file);
        //super.prefetch(); 
    }

    public String toString() {
        return "<AnyFileProxyNode>:" + file;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public String getName() {
        return file.getBasename();
    }

    @Override
    public boolean hasChildren() {
        if (file.isFile()) {
            return false;
        }

        try {
            String[] list = file.list();
            return ((list != null) && (list.length > 0));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    protected ProxyNode doGetParent() throws ProxyException {
        return this.getProxyFactory().doOpenLocation(this.locator.getParent());
    }

    @Override
    public List<? extends ProxyNode> doGetChilds(int offset, int range, LongHolder numChildsLeft)
            throws ProxyException {
        FSPath[] files;
        try {
            files = file.listNodes();
        } catch (IOException e) {
            throw new ProxyException("Couldn't list contents:" + this, e);
        }

        if (files == null) {
            return null;
        }

        ArrayList<FSNodeProxyNode> nodes = new ArrayList<FSNodeProxyNode>(files.length);

        for (int i = 0; i < files.length; i++) {
            nodes.add(createNewNode(files[i]));
        }

        return subrange(nodes, offset, range);
    }

    protected FSNodeProxyNode createNewNode(FSPath fsNode) throws ProxyException {
        return new FSNodeProxyNode(getProxyFactory(), new VRL(fsNode.getURI()), fsNode);
    }

    @Override
    protected String doGetMimeType() throws ProxyException {
        return MimeTypes.getDefault().getMimeType(file.getPathname());
    }

    @Override
    protected boolean doGetIsComposite() throws ProxyException {
        return this.file.isDirectory();
    }

    @Override
    protected String doGetName() {
        return this.getVRL().getBasename();
    }

    @Override
    protected String doGetResourceType() {
        if (file.isFile()) {
            return FSPath.FILE_TYPE;
        } else {
            return FSPath.DIR_TYPE;
        }
    }

    @Override
    protected String doGetResourceStatus() {
        return null;
    }

    @Override
    protected List<String> doGetChildTypes() {
        return new StringList(FSPath.FILE_TYPE, FSPath.DIR_TYPE);
    }

    @Override
    protected List<String> doGetAttributeNames() throws ProxyException {
        return metaFile.getAttributeNames();
    }

    @Override
    protected List<Attribute> doGetAttributes(String[] names) throws ProxyException {
        return metaFile.getAttributes(names);
    }

    @Override
    protected Presentation doGetPresentation() {
        // redirect to meta file 
        return metaFile.getPresentation();
    }

    @Override
    protected String doGetIconURL(String status, int size) throws ProxyException {
        return null;
    }

    @Override
    protected boolean doIsResourceLink() {
        return false;
    }

    @Override
    protected VRL doGetResourceLinkVRL() {
        return null;
    }

    @Override
    protected ProxyNode doCreateNew(String type, String optNewName) throws ProxyException {
        try {
            FSPath newPath = file.resolvePath(optNewName);
            if (StringUtil.equals(type, FSPath.FILE_TYPE)) {
                newPath.create();
                return createNewNode(newPath);
            } else if (StringUtil.equals(type, FSPath.FILE_TYPE)) {
                newPath.mkdir();
                return createNewNode(newPath);
            } else {
                throw new ProxyException("Create: unrecognized type:" + type);
            }
        } catch (FileURISyntaxException e) {
            throw new ProxyException("Invalid location:" + optNewName + "\n" + e.getMessage(), e);
        } catch (IOException e) {
            throw new ProxyException("Couldn't create new " + type + " " + optNewName + "\n"
                    + e.getMessage(), e);
        }
    }

    @Override
    protected void doDelete(boolean recurse) throws ProxyException {
        throw new ProxyException("Won't delete stuff.");
    }

    @Override
    protected ProxyNode doRenameTo(String nameOrNewPath) throws ProxyException {
        throw new ProxyException("Not Implemented.");
    }

    @Override
    protected boolean doExists() throws ProxyException {
        return this.file.exists(LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    protected ResourceConfigInfo doGetResourceConfigInfo() throws ProxyException {
        return null;
    }

    @Override
    protected ResourceConfigInfo doUpdateResourceConfigInfo(ResourceConfigInfo info)
            throws ProxyException {
        return null;
    }

}
