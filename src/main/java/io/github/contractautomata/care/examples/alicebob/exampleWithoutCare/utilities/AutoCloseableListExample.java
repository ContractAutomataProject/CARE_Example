package io.github.contractautomata.care.examples.alicebob.exampleWithoutCare.utilities;

import java.util.ArrayList;

public class AutoCloseableListExample<T  extends AutoCloseable > extends ArrayList<T> implements AutoCloseable {

	private static final long serialVersionUID = 1L;

	@Override
	public void close() {
		this.stream().forEach(el -> {
			if (el != null)
				try {
					el.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		});
		
	}

}
