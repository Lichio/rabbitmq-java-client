//  The contents of this file are subject to the Mozilla Public License
//  Version 1.1 (the "License"); you may not use this file except in
//  compliance with the License. You may obtain a copy of the License
//  at http://www.mozilla.org/MPL/
//
//  Software distributed under the License is distributed on an "AS IS"
//  basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
//  the License for the specific language governing rights and
//  limitations under the License.
//
//  The Original Code is RabbitMQ.
//
//  The Initial Developer of the Original Code is VMware, Inc.
//  Copyright (c) 2011 VMware, Inc.  All rights reserved.
//

package com.rabbitmq.utility;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

public class IntAllocatorTests extends TestCase {

    private static final int TEST_ITERATIONS = 50000;
    private static final int HI_RANGE = 100000;
    private static final int LO_RANGE = 100;
    private IntAllocator iAll = new IntAllocator(LO_RANGE, HI_RANGE);

    private Random rand = new Random(70608L);

    public void testReserveAndFree() throws Exception {
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < TEST_ITERATIONS; ++i) {
            int trial = getTrial(rand);
            if (set.contains(trial)) {
                iAll.free(trial);
                set.remove(trial);
            } else {
                assertTrue("Did not reserve free integer " + trial,
                        iAll.reserve(trial));
                set.add(trial);
            }
        }

        for (int trial : set) {
            assertFalse("Integer " + trial + " not allocated!",
                    iAll.reserve(trial));
        }
    }

    public void testAllocateAndFree() throws Exception {
        Set<Integer> set = new HashSet<Integer>();
        for (int i=0; i < TEST_ITERATIONS; ++i) {
            if (getBool(rand)) {
                int trial = iAll.allocate();
                assertFalse("Already allocated " + trial, set.contains(trial));
                set.add(trial);
            } else {
                if (!set.isEmpty()) {
                    int trial = extract(set);
                    assertFalse("Allocator agreed to reserve " + trial,
                            iAll.reserve(trial));
                    iAll.free(trial);
                }
            }
        }

        for (int trial : set) {
            assertFalse("Integer " + trial + " should be allocated!",
                    iAll.reserve(trial));
        }
    }

    public void testReserveAndFreeBS() throws Exception {
        IntBitSetAllocator ibs = new IntBitSetAllocator(LO_RANGE, HI_RANGE);
        Set<Integer> set = new HashSet<Integer>();
        for (int i = 0; i < TEST_ITERATIONS; ++i) {
            int trial = getTrial(rand);
            if (set.contains(trial)) {
                ibs.free(trial);
                set.remove(trial);
            } else {
                assertTrue("Did not reserve free integer " + trial,
                        ibs.reserve(trial));
                set.add(trial);
            }
        }

        for (int trial : set) {
            assertFalse("Integer " + trial + " not allocated!",
                    ibs.reserve(trial));
        }
    }

    public void testAllocateAndFreeBS() throws Exception {
        IntBitSetAllocator ibs = new IntBitSetAllocator(LO_RANGE, HI_RANGE);
        Set<Integer> set = new HashSet<Integer>();
        for (int i=0; i < TEST_ITERATIONS; ++i) {
            if (getBool(rand)) {
                int trial = ibs.allocate();
                assertFalse("Already allocated " + trial, set.contains(trial));
                set.add(trial);
            } else {
                if (!set.isEmpty()) {
                    int trial = extract(set);
                    assertFalse("Allocator agreed to reserve " + trial,
                            ibs.reserve(trial));
                    ibs.free(trial);
                }
            }
        }

        for (int trial : set) {
            assertFalse("Integer " + trial + " should be allocated!",
                    ibs.reserve(trial));
        }
    }

    public void testToStringBS() throws Exception {
        IntBitSetAllocator ibs = new IntBitSetAllocator(LO_RANGE, HI_RANGE);
        assertEquals("IntBitSetAllocator{allocated = []}", ibs.toString());
        ibs.allocate();
        assertEquals("IntBitSetAllocator{allocated = [100]}", ibs.toString());
        for(int i = 200; i<211; i=i+4) {
            ibs.reserve(i);
            ibs.reserve(i+1);
            ibs.reserve(i+2);
        }
        assertEquals("IntBitSetAllocator{allocated = "
                    + "[100, 200..202, 204..206, 208..210]}"
                    , ibs.toString());
    }

    public void testToString() throws Exception {
        assertEquals("IntAllocator{intervals = [100..100000], unsorted = []}"
                    , iAll.toString());
        iAll.allocate();
        assertEquals("IntAllocator{intervals = [101..100000], unsorted = []}"
                    , iAll.toString());
        iAll.free(100);
        assertEquals("IntAllocator{intervals = [101..100000]"
                    + ", unsorted = [100]}"
                    , iAll.toString());
        for(int i = 200; i<211; i=i+4) {
            iAll.reserve(i);
            iAll.reserve(i+1);
            iAll.reserve(i+2);
        }
        assertEquals("IntAllocator{intervals = "
                    + "[100..199, 203..203, 207..207, 211..100000]"
                    + ", unsorted = []}"
                    , iAll.toString());
    }

    private static int extract(Set<Integer> set) {
        Iterator<Integer> iter = set.iterator();
        int trial = iter.next();
        iter.remove();
        return trial;
    }
    
    private static int getTrial(Random rand) {
        return rand.nextInt(HI_RANGE-LO_RANGE+1) + LO_RANGE;
    }
    
    private static boolean getBool(Random rand) {
        return rand.nextBoolean();
    }
}
