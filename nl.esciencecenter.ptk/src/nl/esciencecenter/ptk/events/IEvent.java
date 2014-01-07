package nl.esciencecenter.ptk.events;

public interface IEvent<SourceT>
{
    
    public SourceT getSource();

}
