/*
 * gleem -- OpenGL2 Extremely Easy-To-Use Manipulators. Copyright (C) 1998-2003 Kenneth B. Russell
 * (kbrussel@alum.mit.edu) Copying, distribution and use of this software in source and binary forms, with or
 * without modification, is permitted provided that the following conditions are met: Distributions of source
 * code must reproduce the copyright notice, this list of conditions and the following disclaimer in the
 * source code header files; and Distributions of binary code must reproduce the copyright notice, this list
 * of conditions and the following disclaimer in the documentation, Read me file, license file and/or other
 * materials provided with the software distribution. The names of Sun Microsystems, Inc. ("Sun") and/or the
 * copyright holder may not be used to endorse or promote products derived from this software without specific
 * prior written permission. THIS SOFTWARE IS PROVIDED "AS IS," WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR
 * IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, NON-INTERFERENCE, ACCURACY OF INFORMATIONAL CONTENT OR NON-INFRINGEMENT,
 * ARE HEREBY EXCLUDED. THE COPYRIGHT HOLDER, SUN AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN
 * NO EVENT WILL THE COPYRIGHT HOLDER, SUN OR SUN'S LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA,
 * OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. YOU ACKNOWLEDGE THAT THIS SOFTWARE IS NOT DESIGNED, LICENSED OR
 * INTENDED FOR USE IN THE DESIGN, CONSTRUCTION, OPERATION OR MAINTENANCE OF ANY NUCLEAR FACILITY. THE
 * COPYRIGHT HOLDER, SUN AND SUN'S LICENSORS DISCLAIM ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR SUCH
 * USES.
 */

package gleem.linalg;

import java.util.Arrays;

/**
 * Arbitrary-length integer vector class. Currently very simple and only
 * supports a few needed operations.
 */
public class Veci {

	private int[] data;

	public Veci(int n) {
		data = new int[n];
	}

	public Veci(Veci arg) {
		data = new int[arg.data.length];
		System.arraycopy(arg.data, 0, data, 0, data.length);
	}

	public int length() {
		return data.length;
	}

	public int get(int i) {
		return data[i];
	}

	public void setComponent(int i, int val) {
		data[i] = val;
	}

	public Vec2f toVec2f() throws DimensionMismatchException {
		if (length() != 2)
			throw new DimensionMismatchException();
		Vec2f out = new Vec2f();
		for (int i = 0; i < 2; i++) {
			out.setComponent(i, get(i));
		}
		return out;
	}

	public Vec3f toVec3f() throws DimensionMismatchException {
		if (length() != 3)
			throw new DimensionMismatchException();
		Vec3f out = new Vec3f();
		for (int i = 0; i < 3; i++) {
			out.setComponent(i, get(i));
		}
		return out;
	}

	public Vecf toVecf() {
		Vecf out = new Vecf(length());
		for (int i = 0; i < length(); i++) {
			out.setComponent(i, get(i));
		}
		return out;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Veci other = (Veci) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}

}
