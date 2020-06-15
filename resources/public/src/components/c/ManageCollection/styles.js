import styled from 'styled-components'

const Style = styled.div`
  & > header {
    height: 12rem;
    padding: 0 3.8rem;

    display: flex;
    align-items: center;
    justify-content: space-between;

    & h6 {
      font-size: 1.8rem;
      font-weight: bold;
    }

    & p {
      color: #a4a4a4;
      margin-top: 1.7rem;
    }
  }
`

export default Style

export const Title = styled.div`
  display: flex;
  align-items: center;
`

export const TitleEdit = styled.input`
  margin-left: -0.3rem;
  font-size: 1.8rem;
  font-weight: bold;
`

export const TitleEditButton = styled.div`
  color: ${props => props.editing ? `#0582CA` : `#a4a4a4;`};
  font-weight: bold;
  cursor: pointer;
  margin-top: 0;
  margin-left: 1rem;
`

export const PublishButton = styled.button`
  color: white;
  font-weight: bold;
  background-color: ${props => props.published ? `#FFBF00` : `#0582CA`};

  letter-spacing: 0.05rem;

  padding: 0.8rem 1.5rem;
  margin-right: 3rem;

  border: none;
  border-radius: 0.3rem;

  cursor: pointer;
  outline: none;
`

export const ArchiveButton = styled.button`
  color: #ff4c4c;
  font-weight: bold;

  letter-spacing: 0.05rem;

  padding: 0;
  background: transparent;

  border: none;
  cursor: pointer;
  outline: none;
`

export const Tab = styled.div`
  background-color: #fafafa;
  overflow-y: scroll;

  border-top: 1px solid #c4c4c4;

  padding: 2rem;

  height: calc(100vh - 24.6rem);
`

export const TabHeader = styled.div`
  position: absolute;
  top: 18rem;

  padding-left: 2rem;

  height: 2.5rem;

  & > button {
    padding: 0;
    width: 10rem;
    background: transparent;
    height: 2.5rem;

    border: none;
    outline: none;
    cursor: pointer;
  }
`

export const Selector = styled.div`
  position: absolute;

  bottom: 0;
  left: ${props => props.isContent ? `2rem` : `12rem`};

  transition: left 0.3s ease-in-out;

  height: 0.25rem;
  width: 10rem;

  background-color: #0582ca;
`

export const NewContent = styled.button`
  width: calc(100% - 4rem);
  height: 6.1rem;

  margin: 2rem;

  border: none;
  border-radius: 0.3rem;

  background-color: #eee;

  display: flex;
  align-items: center;
  justify-content: center;

  outline: none;
  cursor: pointer;
`

export const Icon = styled.div`
  background: url(${props => props.src}) center no-repeat;
  background-size: contain;

  height: 2rem;
  width: 2rem;
`
