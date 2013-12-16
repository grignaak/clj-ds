package persistent;

import java.io.Serializable;

public abstract class AbstractBuilder {
    
    public static class Owner implements Serializable {
        private static final long serialVersionUID = 1L;
        private transient volatile Thread owner;

        public Owner() {
            this.owner = Thread.currentThread();
        }
        
        public <T> T built(T t) {
            owner = null;
            return t;
        }

        protected void ensureEditable() {
            final Thread thread = owner;
            if (thread == Thread.currentThread())
                return;
            if (thread != null)
                throw new IllegalAccessError("Builder used by non-owner thread");
            throw new IllegalAccessError("Builder used after persistent! call");
        }

        public static Owner none() {
            Owner owner = new Owner();
            return owner.built(owner);
        }
    }
    
    protected final Owner owner;
    
    protected AbstractBuilder(Owner owner) {
        this.owner = owner;
    }
}
