package com.bbn.bue.common.evaluation;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.difference;

/**
 * A {@link ProvenancedAlignment} based on grouping items into equivalence classes. The items
 * aligned are the equivalence classes and the provenances are the original items.
 */
@Beta
public final class EquivalenceBasedProvenancedAlignment<EqClassT, LeftT, RightT>
    implements ProvenancedAlignment<EqClassT, LeftT, EqClassT, RightT> {

  private final ImmutableMultimap<EqClassT, LeftT> leftEquivalenceClassesToProvenances;
  private final ImmutableMultimap<EqClassT, RightT> rightEquivalenceClassesToProvenances;

  private EquivalenceBasedProvenancedAlignment(
      final Multimap<? extends EqClassT, ? extends LeftT> leftEquivalenceClassesToProvenances,
      final Multimap<? extends EqClassT, ? extends RightT> rightEquivalenceClassesToProvenances) {
    this.leftEquivalenceClassesToProvenances = ImmutableSetMultimap
        .copyOf(leftEquivalenceClassesToProvenances);
    this.rightEquivalenceClassesToProvenances = ImmutableSetMultimap.copyOf(
        rightEquivalenceClassesToProvenances);
  }

  // package-private
  static <EqClassT, LeftProvT, RightProvT> EquivalenceBasedProvenancedAlignment<EqClassT, LeftProvT, RightProvT> fromEquivalenceClassMaps(
      final Multimap<? extends EqClassT, ? extends LeftProvT> leftEquivalenceClassesToProvenances,
      final Multimap<? extends EqClassT, ? extends RightProvT> rightEquivalenceClassesToProvenances) {
    return new EquivalenceBasedProvenancedAlignment<EqClassT, LeftProvT, RightProvT>(
        leftEquivalenceClassesToProvenances, rightEquivalenceClassesToProvenances);
  }

  @Override
  public Collection<LeftT> provenancesForLeftItem(final EqClassT item) {
    return leftEquivalenceClassesToProvenances.get(item);
  }

  @Override
  public Collection<RightT> provenancesForRightItem(final EqClassT item) {
    return rightEquivalenceClassesToProvenances.get(item);
  }

  @Override
  public Set<EqClassT> leftUnaligned() {
    return difference(leftEquivalenceClassesToProvenances.keySet(),
        rightEquivalenceClassesToProvenances.keySet());
  }

  @Override
  public Set<EqClassT> rightUnaligned() {
    return difference(rightEquivalenceClassesToProvenances.keySet(),
        leftEquivalenceClassesToProvenances.keySet());
  }

  @Override
  public Set<EqClassT> leftAligned() {
    return leftEquivalenceClassesToProvenances.keySet();
  }

  @Override
  public Set<EqClassT> rightAligned() {
    return rightEquivalenceClassesToProvenances.keySet();
  }

  // if it appears in the multimap, it's got to be an EqClassT
  @SuppressWarnings("unchecked")
  @Override
  public Collection<EqClassT> alignedToRightItem(final Object rightItem) {
    if (rightEquivalenceClassesToProvenances.containsKey(rightItem)) {
      return ImmutableList.of((EqClassT) rightItem);
    } else {
      return ImmutableList.of();
    }
  }

  // if it appears in the multimap, it's got to be an EqClassT
  @SuppressWarnings("unchecked")
  @Override
  public Collection<EqClassT> alignedToLeftItem(final Object leftItem) {
    if (rightEquivalenceClassesToProvenances.containsKey(leftItem)) {
      return ImmutableList.of((EqClassT) leftItem);
    } else {
      return ImmutableList.of();
    }
  }

  @Override
  public Set<EqClassT> allLeftItems() {
    return leftEquivalenceClassesToProvenances.keySet();
  }

  @Override
  public Set<EqClassT> allRightItems() {
    return rightEquivalenceClassesToProvenances.keySet();
  }
}
