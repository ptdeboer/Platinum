/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.vbrowser.viewers.events;

import nl.esciencecenter.ptk.events.IEvent;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.util.HashMap;
import java.util.Map;

public class ViewerEvent implements IEvent<ViewerEventType> {

    public static ViewerEvent createStartedEvent(ViewerEventSource source) {
        return new ViewerEvent(source, ViewerEventType.VIEWER_STARTED);
    }

    public static ViewerEvent createStoppedEvent(ViewerEventSource source) {
        return new ViewerEvent(source, ViewerEventType.VIEWER_STARTED);
    }

    public static ViewerEvent createDisposedEvent(ViewerEventSource source) {
        return new ViewerEvent(source, ViewerEventType.VIEWER_DISPOSED);
    }

    public static ViewerEvent createHyperLinkEvent(ViewerEventSource source, ViewerEventType eventType, VRL vrl) {
        ViewerEvent event = new ViewerEvent(source, eventType);
        event.setVrl(vrl);
        return event;
    }

    public static ViewerEvent createHyperLinkEvent(ViewerEventSource source, ViewerEventType eventType, VRL parent,
                                                   VRL vrl) {
        ViewerEvent event = new ViewerEvent(source, eventType);
        event.setParentVrl(parent);
        event.setVrl(vrl);
        return event;
    }

    public static ViewerEvent createExceptionEvent(ViewerEventSource source, String message, Throwable ex) {
        ViewerEvent event = new ViewerEvent(source, ViewerEventType.VIEWER_ERROR);
        event.eventProperties = new HashMap<String, String>();
        event.eventProperties.put("exception", ex.getMessage());
        return event;
    }

    // ===============
    //
    // ===============

    protected ViewerEventType eventType;

    protected VRL optionalVrl;

    protected VRL parentVrl;

    protected ViewerEventSource eventSource;

    protected Map<String, String> eventProperties = null;

    public VRL getVrl() {
        return optionalVrl;
    }

    public void setVrl(VRL optionalVrl) {
        this.parentVrl = optionalVrl;
    }

    public VRL getParentVrl() {
        return parentVrl;
    }

    public void setParentVrl(VRL optionalVrl) {
        this.optionalVrl = optionalVrl;
    }

    public ViewerEvent(ViewerEventSource source, ViewerEventType type) {
        this.eventSource = source;
        this.eventType = type;
    }

    public ViewerEvent(ViewerEventSource source, ViewerEventType type, Map<String, String> eventProperties) {
        this.eventSource = source;
        this.eventType = type;
        this.eventProperties = eventProperties;
    }

    @Override
    public ViewerEventSource getEventSource() {
        return this.eventSource;
    }

    @Override
    public ViewerEventType getEventType() {
        return this.eventType;
    }

    @Override
    public String toString() {
        return "ViewerEvent[eventType=" + eventType + ", optionalVrl=" + optionalVrl
                + ", parentVrl=" + parentVrl + ", eventSource=" + eventSource + "]";
    }

    protected Map<String, String> getEvents() {
        return this.eventProperties;
    }


}
