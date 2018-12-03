package javax.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is being provided as a no-op shim since JOTM depends on this class but
 * it does nothing with it.  This class was removed from the JDK for Java 11
 * when CORBA was removed.
 */
public class PortableRemoteObject {

    public static void exportObject(Remote obj)
            throws RemoteException {

    }

    public static Remote toStub (Remote obj)
            throws NoSuchObjectException {

        return null;
    }

    public static void unexportObject(Remote obj)
            throws NoSuchObjectException {

    }

    public static java.lang.Object narrow ( java.lang.Object narrowFrom,
                                            java.lang.Class narrowTo)
            throws ClassCastException {

        return null;

    }

    public static void connect (Remote target, Remote source)
            throws RemoteException {

    }
}
