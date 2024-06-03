const ADDITIONAL_CONSTRAINTS = "additionalConstraints";

const additionalConstraint = ref(useSessionStorage(ADDITIONAL_CONSTRAINTS, ""));

export default () => {

    const getAdditionalConstraint = () => {
        return additionalConstraint
    }

    const getConstraintList = () => {
        return additionalConstraint.value && additionalConstraint.value.length > 0 ? [additionalConstraint.value] : [];
    };

    const clearAdditionalConstraints = () => {
        sessionStorage.removeItem(ADDITIONAL_CONSTRAINTS);
        additionalConstraint.value = null;
    };

    return { getAdditionalConstraint, getConstraintList, clearAdditionalConstraints };
};
